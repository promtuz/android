package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.promtuz.chat.data.remote.proto.IdentityPacket
import com.promtuz.chat.data.repository.UserRepository
import com.promtuz.chat.domain.model.Identity
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.utils.media.ImageUtils
import com.promtuz.chat.utils.serialization.AppCbor
import com.promtuz.chat.utils.serialization.cborDecode
import com.promtuz.core.Crypto
import dev.shreyaspatil.capturable.controller.CaptureController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import tech.kwik.core.QuicClientConnection
import java.nio.ByteBuffer
import java.nio.ByteOrder

@OptIn(ExperimentalSerializationApi::class)
class ShareIdentityVM(
    private val userRepository: UserRepository,
    private val application: Application,
    private val keyManager: KeyManager,
    private val imgUtils: ImageUtils,
    private val crypto: Crypto,
    private val appVM: AppVM,
) : ViewModel() {
    private val context: Context get() = application.applicationContext

    private var _qrData = MutableStateFlow<ByteArray?>(null)
    val qrData = _qrData.asStateFlow()

    private lateinit var keyPair: Pair<ByteArray, ByteArray>

    suspend fun init() = coroutineScope {
        keyPair = crypto.getStaticKeypair()

        // TODO: open local connection, wait on it
        //  ask for public addr from relay
        //  swap the port with local connection
        //  send that addr on qr

        val conn = appVM.conn ?: waitForRelay()
        val relayAddr = conn.serverAddress

        val verifyKey =
            ByteArray(32) // keyManager.getSecretKey().toSigningKey().getVerificationKey()
        val publicKey = keyManager.getPublicKey()

        val identity = Identity(
            publicKey,
            keyPair.second,
            verifyKey,
            "${relayAddr.hostString}:${relayAddr.port}",
            userRepository.getCurrentUser().nickname
        )

        _qrData.value = AppCbor.instance.encodeToByteArray(identity)

        // listening for identity ping

        val stream = conn.createStream(true)

        while (true) {
            val packet = stream.inputStream.readNBytes(
                ByteBuffer.wrap(stream.inputStream.readNBytes(4)).order(ByteOrder.BIG_ENDIAN).int
            )

            // @formatter:off
            val data =
                cborDecode<IdentityPacket.AddMe>(packet)
            // @formatter:on

            when (data) {
                is IdentityPacket.AddMe -> {

                }

                null -> {}
            }
        }
    }

    private suspend fun waitForRelay(): QuicClientConnection {
        while (true) {
            appVM.conn?.let { return it }
            delay(100) // non-blocking wait
        }
    }

    init {
        viewModelScope.launch {
            init()
        }
    }


    fun shareQrCode(
        captureController: CaptureController, shareCallback: (shareIntent: Intent) -> Unit
    ) {
        viewModelScope.launch {
            val bitmapAsync = captureController.captureAsync()
            try {
                val bitmap = bitmapAsync.await()
                val uri = imgUtils.saveImageCache(bitmap.asAndroidBitmap())

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setType("image/png")
                }

                shareCallback(Intent.createChooser(shareIntent, "Share QR Code"))
            } catch (error: Exception) {
                Toast.makeText(
                    context, "Failed to generate QR Image: ${error.message}", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}