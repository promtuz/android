package com.promtuz.chat.data.remote

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.promtuz.chat.R
import com.promtuz.chat.data.remote.dto.ClientResponseDto
import com.promtuz.chat.data.remote.dto.ResolvedRelays
import com.promtuz.chat.data.remote.realtime.cborDecode
import com.promtuz.chat.domain.model.ResolverSeeds
import com.promtuz.chat.presentation.state.ConnectionState
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

//sealed class ConnectionError : Throwable() {
//    class NoInternet : ConnectionError()
//    class ServerUnreachable : ConnectionError()
//    class Timeout : ConnectionError()
//
//    data class HandshakeFailed(val reason: String) : ConnectionError()
//    data class Unknown(val exception: Exception) : ConnectionError()
//}

class QuicClient(
    private val application: Application,
    private val keyManager: KeyManager,
    private val crypto: Crypto
) : KoinComponent {
    private val context: Context get() = application.applicationContext
    private var _status = mutableStateOf<ConnectionState>(ConnectionState.Idle)
    val status: State<ConnectionState> get() = _status

    private val log = Timber.tag("QuicClient")

    suspend fun resolve(): Result<ResolvedRelays> = withContext(Dispatchers.IO) {
        val seeds = context.resources.openRawResource(R.raw.resolver_seeds)
            .readBytes()
            .decodeToString()
            .let { Json.decodeFromString<ResolverSeeds>(it).seeds }

        if (seeds.isEmpty()) {
            _status.value = ConnectionState.Failed
        } else {
            for (seed in seeds) {
                try {
                    _status.value = ConnectionState.Resolving

                    val conn = QuicClientConnection.newBuilder()
                        .customTrustManager(TrustManager.pinned(context))
                        .version(QuicConnection.QuicVersion.V1)
                        .uri(URI("https://${seed.host}:${seed.port}"))
                        .applicationProtocol("client/$PROTOCOL_VERSION")
                        .build()

                    conn.connect()

                    val stream = conn.createStream(true)

                    /// CBOR Representation of `ClientRequest::GetRelays()`, an empty struct with name "GetRelays"
                    val getRelays = byteArrayOf(
                        161.toByte(), 105, 71, 101, 116, 82, 101, 108, 97, 121, 115,
                        128.toByte()
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
                    _status.value = ConnectionState.Failed
                    log.e(e, "Failed to Resolve")
                    return@withContext Result.failure(e)
                }
            }
        }
        return@withContext Result.failure(Throwable("N0_RELAYS_FOR_YA"))
    }

//    suspend fun connect(addr: InetSocketAddress): Result<Connection> =

//    private lateinit var sharedSecret: ByteArray
//
//    private fun hasInternetConnectivity(context: Context): Boolean {
//        val connectivityManager =
//            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//        val network = connectivityManager.activeNetwork ?: return false
//        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
//
//        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && capabilities.hasCapability(
//            NetworkCapabilities.NET_CAPABILITY_VALIDATED
//        )
//    }
//
//    suspend fun connect(context: Context, addr: InetSocketAddress): Result<Unit> =
//        withContext(Dispatchers.IO) {
//
//            if (!hasInternetConnectivity(context)) {
//                return@withContext Result.failure(ConnectionError.NoInternet)
//            }
//            try {
//                connection?.close(1001, "Reinitiating Connection")
//                val conn = QuicClientConnection.newBuilder()
//                    .customTrustManager(TrustManager.pinned(context))
//                    .version(QuicConnection.QuicVersion.V1)
//                    .uri()
//                    .applicationProtocol("client/$PROTOCOL_VERSION")
//                    .maxIdleTimeout(Duration.ofHours(1))
//                    .build()
//
//                connection = conn
//                _status.value = ConnectionStatus.Connecting
//                conn.connect()
//
//                Result.success(Unit)
//            } catch (e: Exception) {
//                _status.value = ConnectionStatus.HandshakeFailed
//                Timber.tag("QuicClient").d("Failed to Connect : $e")
//                Result.failure(e)
//            }
//        }
//
//    /**
//     * @throws IOException if sharedSecret is null
//     */
//    @OptIn(ExperimentalSerializationApi::class)
//    fun prepareMsg(
//        ev: ClientEvents,
//        ad: ByteArray = ByteArray(0),
//        sharedSecret: ByteArray? = if (::sharedSecret.isInitialized) this.sharedSecret else null
//    ): ByteArray {
//        if (sharedSecret == null) {
//            throw IOException("prepareMsg was called without proper shared key")
//        }
//
//        val key = crypto.deriveSharedKey(
//            sharedSecret, Salts.EVENT, Info.CLIENT_EVENT_CL_TO_SV
//        )
//
//        val data = crypto.encryptData(AppCbor.instance.encodeToByteArray(ev), key, ad)
//
//        return AppCbor.instance.encodeToByteArray(data)
//    }
}