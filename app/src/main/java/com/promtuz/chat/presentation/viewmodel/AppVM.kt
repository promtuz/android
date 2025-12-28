package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.promtuz.chat.R
import com.promtuz.chat.domain.model.Chat
import com.promtuz.chat.navigation.AppNavigator
import com.promtuz.chat.navigation.Routes
import com.promtuz.chat.security.KeyManager
import com.promtuz.core.API
import com.promtuz.core.events.InternalEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import com.promtuz.chat.presentation.state.ConnectionState as CS

class AppVM(
    private val application: Application, private val keyManager: KeyManager, private val api: API
) : ViewModel() {
    private val context: Context get() = application.applicationContext

    var activeChatUser: Chat? = null

    var backStack = NavBackStack<NavKey>(Routes.App)
    val navigator = AppNavigator(backStack)

    private val _dynamicTitle = MutableStateFlow(context.resources.getString(R.string.app_name))
    val dynamicTitle: StateFlow<String> = _dynamicTitle.asStateFlow()

    init {
        viewModelScope.launch {
            var titleResetJob: Job? = null

            api.eventsFlow.filterIsInstance<InternalEvent.Connection>().map { it.state }
                .distinctUntilChanged().collect { state ->
                    titleResetJob?.cancel()

                    _dynamicTitle.value = when (state) {
                        CS.Idle -> context.resources.getString(R.string.app_name)
                        CS.Connecting, CS.Failed, CS.Handshaking, CS.Reconnecting, CS.Resolving, CS.NoInternet -> context.resources.getString(
                            state.text
                        )

                        CS.Connected, CS.Disconnected -> {
                            context.resources.getString(state.text).also {
                                titleResetJob = launch {
                                    delay(1200)
                                    _dynamicTitle.value =
                                        context.resources.getString(R.string.app_name)
                                }
                            }
                        }
                    }
                }
        }
    }

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
            api.connect(context)
        }
    }
}