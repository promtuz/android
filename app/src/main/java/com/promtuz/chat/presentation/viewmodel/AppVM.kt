package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.data.remote.dto.RelayDescriptor
import com.promtuz.chat.domain.model.Chat
import com.promtuz.chat.navigation.AppNavigator
import com.promtuz.chat.navigation.Routes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppVM(
    private val application: Application, private val quicClient: QuicClient
) : ViewModel() {
    private val context: Context get() = application.applicationContext

    var activeChatUser: Chat? = null

    var backStack = NavBackStack<NavKey>(Routes.App)
    val navigator = AppNavigator(backStack)

    fun openChat(identityKey: Chat) {
        activeChatUser = identityKey
        navigator.push(Routes.Chat)
    }

    init {
        viewModelScope.launch {
            val real = quicClient.resolve()

            real.onSuccess { resolved ->
                // TODO: store these relays

                for (relay in resolved.relays) {
                    connectToRelay(relay).onSuccess {
                        break
                    }
                }
            }
        }
    }


    private suspend fun connectToRelay(relay: RelayDescriptor): Result<Unit> =
        withContext(Dispatchers.IO) {
            quicClient.connect(relay).map { conn ->
                // prolly create some handler class instance using conn & save it inside vm
            }
        }
}