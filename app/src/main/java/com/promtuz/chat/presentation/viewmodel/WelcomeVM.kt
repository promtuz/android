package com.promtuz.chat.presentation.viewmodel

//import com.promtuz.chat.data.remote.ConnectionError
import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.promtuz.chat.data.local.databases.AppDatabase
import com.promtuz.chat.presentation.state.WelcomeField
import com.promtuz.chat.presentation.state.WelcomeStatus
import com.promtuz.chat.presentation.state.WelcomeUiState
import org.koin.core.component.KoinComponent

class WelcomeVM(
    private val application: Application,
    appDatabase: AppDatabase
) : ViewModel(), KoinComponent {
//    private val context: Context get() = application.applicationContext
//    private val users = appDatabase.userDao()

    private val _uiState = mutableStateOf(
        WelcomeUiState(
            "", WelcomeStatus.Normal, null
        )
    )
    val uiState: State<WelcomeUiState> = _uiState

    fun <T> onChange(field: WelcomeField, value: T) {
        _uiState.value = when (field) {
            WelcomeField.Nickname -> _uiState.value.copy(nickname = value as String)
            WelcomeField.Error -> _uiState.value.copy(errorText = value as String?)
            WelcomeField.Status -> _uiState.value.copy(status = value as WelcomeStatus)
        }
    }

    fun `continue`(onSuccess: () -> Unit) {
        onChange(WelcomeField.Status, WelcomeStatus.Generating)

        // TODO: reimplement in `libcore`
    }
}