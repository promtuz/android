package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.promtuz.chat.utils.media.ImageUtils
import dev.shreyaspatil.capturable.controller.CaptureController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
class ShareIdentityVM(
    private val application: Application,
    private val imgUtils: ImageUtils
) : ViewModel() {
    private val context: Context get() = application.applicationContext

    private var _qrData = MutableStateFlow<ByteArray?>(null)
    val qrData = _qrData.asStateFlow()

    suspend fun init() = coroutineScope {
        // TODO: TO BE REIMPLEMENTED IN `libcore`
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