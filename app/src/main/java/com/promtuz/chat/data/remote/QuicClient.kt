package com.promtuz.chat.data.remote

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.promtuz.chat.R
import com.promtuz.chat.data.remote.dto.ClientResponseDto
import com.promtuz.chat.data.remote.dto.RelayDescriptor
import com.promtuz.chat.data.remote.dto.ResolvedRelays
import com.promtuz.chat.data.remote.dto.bytes
import com.promtuz.chat.data.remote.proto.HandshakeProto
import com.promtuz.chat.data.remote.proto.expectChallenge
import com.promtuz.chat.data.remote.realtime.cborDecode
import com.promtuz.chat.domain.model.ResolverSeeds
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.security.TrustManager
import com.promtuz.core.Crypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import tech.kwik.core.QuicClientConnection
import tech.kwik.core.QuicConnection
import timber.log.Timber
import java.net.URI
import com.promtuz.chat.presentation.state.ConnectionState as ConnState

private const val PROTOCOL_VERSION = 1

fun u32size(len: Int): ByteArray {
    val size: UInt = len.toUInt()
    val sizeBytes = ByteArray(4)
    sizeBytes[0] = (size shr 24).toByte()
    sizeBytes[1] = (size shr 16).toByte()
    sizeBytes[2] = (size shr 8).toByte()
    sizeBytes[3] = size.toByte()
    return sizeBytes
}

fun framePacket(packet: ByteArray): ByteArray {
    return u32size(packet.size) + packet
}

class QuicClient(
    private val application: Application,
    private val keyManager: KeyManager,
    private val crypto: Crypto
) : KoinComponent {
    private val context: Context get() = application.applicationContext
    private var _status = mutableStateOf<ConnState>(ConnState.Idle)
    val status: State<ConnState> get() = _status

    private val log = Timber.tag("QuicClient")

    private fun setState(state: ConnState) {
        _status.value = state
    }

    init {
        if (!hasInternetConnectivity(context)) {
            log.d("Internet Connection Unavailable")
        }
    }

    suspend fun resolve(): Result<ResolvedRelays> = withContext(Dispatchers.IO) {
        if (!hasInternetConnectivity(context)) return@withContext Result.failure(Throwable("No Internet"))

        val seeds =
            context.resources.openRawResource(R.raw.resolver_seeds).readBytes().decodeToString()
                .let { Json.decodeFromString<ResolverSeeds>(it).seeds }

        if (seeds.isEmpty()) {
            setState(ConnState.Failed)
        } else {
            for (seed in seeds) {
                try {
                    setState(ConnState.Resolving)

                    val conn = QuicClientConnection.newBuilder()
                        .customTrustManager(TrustManager.pinned(context))
                        .version(QuicConnection.QuicVersion.V1).serverName(seed.id)
                        .uri(URI("https://${seed.host}:${seed.port}"))
                        .applicationProtocol("client/$PROTOCOL_VERSION").build()

                    conn.connect()

                    val stream = conn.createStream(true)

                    /// CBOR Representation of `ClientRequest::GetRelays()`, an empty struct with name "GetRelays"
                    val getRelays = byteArrayOf(
                        161.toByte(), 105, 71, 101, 116, 82, 101, 108, 97, 121, 115, 128.toByte()
                    )
                    stream.outputStream.write(getRelays)
                    stream.outputStream.close()

                    val bytes = stream.inputStream.readAllBytes()
                    val res = cborDecode<ClientResponseDto>(bytes)

                    conn.close()

                    if (res != null) {
                        return@withContext Result.success(res.content)
                    }
                } catch (e: Exception) {
                    setState(ConnState.Failed)
                    log.e(e, "Failed to Resolve")
                    return@withContext Result.failure(e)
                }
            }
        }
        return@withContext Result.failure(Throwable("N0_RELAYS_FOR_YA"))
    }

    suspend fun connect(relay: RelayDescriptor): Result<QuicClientConnection> = withContext(
        Dispatchers.IO
    ) {
        if (!hasInternetConnectivity(context)) return@withContext Result.failure(Throwable("No Internet"))

        try {
            if (status.value == ConnState.Failed) setState(ConnState.Reconnecting)
            else setState(ConnState.Connecting)

            val conn =
                QuicClientConnection.newBuilder().customTrustManager(TrustManager.pinned(context))
                    .version(QuicConnection.QuicVersion.V1).serverName(relay.id)
                    .uri(URI("https://${relay.addr.hostName}:${relay.addr.port}"))
                    .applicationProtocol("client/$PROTOCOL_VERSION").build()
            conn.connect()

            setState(ConnState.Handshaking)

            // HANDSHAKE BEGIN

            val stream = conn.createStream(true)

            val ipk = keyManager.getPublicKey()
            val (esk, epk) = crypto.getEphemeralKeypair()

            val clientHello = HandshakeProto.ClientHello(ipk.bytes(), epk.bytes())
            stream.outputStream.write(clientHello.toBytes())

            val challengeBytes = stream.inputStream.readNBytes(0x41) // 65 bytes
            val challenge = HandshakeProto.fromBytes(challengeBytes, true).expectChallenge()

            val dh = crypto.ephemeralDiffieHellman(esk, challenge.epk.bytes)
            val key = crypto.deriveSharedKey(dh, ByteArray(32) { 0 }, "handshake.challenge.key")

            val proof = crypto.decryptData(
                cipher = challenge.ct.bytes,
                nonce = ByteArray(12) { 0 },
                key = key,
                ad = epk + challenge.epk.bytes
            )

            val clientProof = HandshakeProto.ClientProof(proof.bytes())
            stream.outputStream.write(clientProof.toBytes())

            val serverResponseBytes = stream.inputStream.readAllBytes()

            when (val serverResponse = HandshakeProto.fromBytes(serverResponseBytes, true)) {
                is HandshakeProto.ServerAccept -> {
                    log.d("Server Accepted at : ${serverResponse.timestamp}")

                    setState(ConnState.Connected)
                }

                is HandshakeProto.ServerReject -> {
                    log.d("Server Rejected with Reason : ${serverResponse.reason}")

                    setState(ConnState.Failed)
                }

                else -> error("Unknown Server Response")
            }

            // HANDSHAKE END

            Result.success(conn)
        } catch (e: Exception) {
            setState(ConnState.Failed)

            log.e(e, "Failed to Connect")

            return@withContext Result.failure(e)
        }
    }

    private fun hasInternetConnectivity(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_VALIDATED
        ).also {
            if (!it) setState(ConnState.Offline)
        }
    }
}