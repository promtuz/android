package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.promtuz.chat.domain.model.Chat
import com.promtuz.chat.navigation.AppNavigator
import com.promtuz.chat.navigation.Routes
import com.promtuz.chat.security.KeyManager
import com.promtuz.core.API
import com.promtuz.core.events.InternalEvent
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class AppVM(
    private val application: Application,
    private val keyManager: KeyManager,
    private val api: API
) : ViewModel() {
    private val context: Context get() = application.applicationContext

    var activeChatUser: Chat? = null

    var backStack = NavBackStack<NavKey>(Routes.App)
    val navigator = AppNavigator(backStack)

    val connStateFlow = api.eventsFlow.filterIsInstance<InternalEvent.Connection>().map { it.state }
        .distinctUntilChanged()

    companion object {
        private const val TAG = "AppVM"
        private val log = { Timber.tag(TAG) }
    }

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

        viewModelScope.launch {
            api.connect(context, keyManager.getPublicKey(), keyManager.getSecretKey())
        }
    }
}