package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.promtuz.chat.R
import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.data.remote.dto.RelayDescriptor
import com.promtuz.chat.data.remote.proto.MiscPacket
import com.promtuz.chat.data.remote.proto.pack
import com.promtuz.chat.domain.model.Chat
import com.promtuz.chat.navigation.AppNavigator
import com.promtuz.chat.navigation.Routes
import com.promtuz.core.API
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.kwik.core.QuicClientConnection
import timber.log.Timber

class AppVM(
    private val application: Application, private val quicClient: QuicClient, private val api: API
) : ViewModel() {
    private val context: Context get() = application.applicationContext

    var activeChatUser: Chat? = null

    var backStack = NavBackStack<NavKey>(Routes.App)
    val navigator = AppNavigator(backStack)

    var conn: QuicClientConnection? = null
        private set


    companion object {
        private const val TAG = "AppVM"
        private val log = { Timber.tag(TAG) }
    }

    /**
     * Public address of client from the POV of server
     */
    var pubaddr: String? = null
        private set

    var connecting = false

    fun openChat(identityKey: Chat) {
        activeChatUser = identityKey
        navigator.push(Routes.Chat)
    }

    init {
        api.initApi(context)

        connection()
    }

    fun connection() {
        if (connecting) return

        api.resolve(context)

//        R.raw.root_ca

        viewModelScope.launch {
            connecting = true


//            val real = quicClient.resolve()
//
//            real.onSuccess { resolved ->
//                // TODO: store these relays
//
//                for (relay in resolved.relays) {
//                    connectToRelay(relay).onSuccess {
//                        break
//                    }
//                }
//            }

            connecting = false
        }
    }


    private suspend fun reqPublicAddress(conn: QuicClientConnection) {
        val stream = conn.createStream(true)

        val req = MiscPacket.PubAddressReq(false)
        stream.outputStream.write(req.pack())

        val out = stream.inputStream.readAllBytes()

        log().d("GOT RESPONSE : ${out.toHexString()}")
    }


    private suspend fun connectToRelay(relay: RelayDescriptor): Result<Unit> =
        withContext(Dispatchers.IO) {
            quicClient.connect(relay).map { conn ->
                this@AppVM.conn = conn


                //
                // prolly create some handler class instance using conn & save it inside vm
            }
        }
}