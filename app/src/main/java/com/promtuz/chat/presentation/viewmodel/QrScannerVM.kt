package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import com.promtuz.chat.data.remote.proto.IdentityPacket
import com.promtuz.chat.data.remote.proto.pack
import com.promtuz.chat.data.repository.UserRepository
import com.promtuz.chat.domain.model.Identity
import com.promtuz.chat.presentation.state.PermissionState
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.utils.serialization.cborDecode
import com.promtuz.core.Crypto
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class QrScannerVM(
    private val userRepository: UserRepository,
    private val application: Application,
    private val keyManager: KeyManager,
    private val crypto: Crypto,
    private val appVM: AppVM,
) : ViewModel() {
    private val context: Context get() = application.applicationContext
    private val log = Timber.tag("QrScannerVM")

    var imageAnalysis =
        ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

    private val _isCameraAvailable = MutableStateFlow(false)
    val isCameraAvailable = _isCameraAvailable.asStateFlow()

    private val _cameraPermissionState = MutableStateFlow(PermissionState.NotRequested)
    val cameraPermissionState = _cameraPermissionState.asStateFlow()

    private val _cameraProviderState = MutableStateFlow<ProcessCameraProvider?>(null)
    val cameraProviderState = _cameraProviderState.asStateFlow()

    private val _selectedIdentity = MutableStateFlow<Identity?>(null)
    val selectedIdentity = _selectedIdentity.asStateFlow()

    private val _identities = MutableStateFlow<List<Identity>>(emptyList())
    val identities = _identities.asStateFlow()

    fun setCameraProvider(provider: ProcessCameraProvider) {
        _cameraProviderState.value = provider
    }

    fun handleCameraPermissionRequest(isGranted: Boolean) {
        if (isGranted) {
            _cameraPermissionState.value = PermissionState.Granted
        } else {
            _cameraPermissionState.value = PermissionState.Denied
        }
    }

    fun makeCameraAvailable() {
        _isCameraAvailable.value = true
    }

    fun handleScannedBarcodes(barcodes: List<Barcode>) = viewModelScope.launch {
        _identities.value = (barcodes.mapNotNull { barcode ->
            barcode.rawBytes?.let { bytes ->
                cborDecode<Identity>(bytes).also {
                    it?.let { log.d("DETECTED IDENTITY $it") }
                }
            } ?: return@mapNotNull null
        }).distinctBy { it }
    }

    fun dismissIdentity() {
        _selectedIdentity.value = null
        _identities.update { emptyList() }
    }

    fun saveUserIdentity(userIdentity: Identity) {
        _selectedIdentity.value = userIdentity
    }


    /// logic for adding user

    private lateinit var keyPair: Pair<ByteArray, ByteArray>

//    suspend fun connect(identity: Identity) = coroutineScope {
//
//        val conn = appVM.conn ?: return@coroutineScope
//
//
//        // PingIdentity contains almost same data as [identity]
//        // but contains hash of [identity]
//        // { ipk, epk, vfk, hash, nickname }
//        val ipk = keyManager.getPublicKey()
//        val verifyKey = keyManager.getSecretKey().toSigningKey().getVerificationKey()
//
//        keyPair = crypto.getStaticKeypair()
//
//        val pingIdentity = IdentityPacket.AddMe(
//            ipk, keyPair.second, verifyKey, userRepository.getCurrentUser().nickname
//        )
//
//        pingIdentity.pack()
//    }
}