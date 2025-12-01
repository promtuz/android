package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.promtuz.chat.data.repository.UserRepository
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.utils.media.ImageUtils
import com.promtuz.chat.utils.serialization.AppCbor
import com.promtuz.core.Crypto
import dev.shreyaspatil.capturable.controller.CaptureController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray
import java.net.InetSocketAddress

@Serializable
data class TempIdentity(
    val ipk: ByteArray, val epk: ByteArray, val vfk: ByteArray, val addr: String?
) {
    override fun equals(other: Any?) = TODO()
    override fun hashCode() = TODO()
}

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

        val relayAddr = appVM.conn?.serverAddress
            ?: waitForRelay()

        val verifyKey = keyManager.getSecretKey().toSigningKey().getVerificationKey()
        val publicKey = keyManager.getPublicKey()

        val identity = TempIdentity(
            publicKey,
            keyPair.second,
            verifyKey,
            "${relayAddr.hostString}:${relayAddr.port}"
        )

        _qrData.value = AppCbor.instance.encodeToByteArray(identity)
    }

    private suspend fun waitForRelay(): InetSocketAddress {
        while (true) {
            appVM.conn?.serverAddress?.let { return it }
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