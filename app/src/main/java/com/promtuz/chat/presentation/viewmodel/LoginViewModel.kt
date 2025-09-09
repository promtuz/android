package com.promtuz.chat.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.promtuz.chat.data.remote.api.ApiResult
import com.promtuz.chat.data.repository.AuthRepositoryImpl
import com.promtuz.chat.presentation.state.LoginField
import com.promtuz.chat.presentation.state.LoginStatus
import com.promtuz.chat.presentation.state.LoginUiState
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepo: AuthRepositoryImpl) : ViewModel() {
    private val _uiState = mutableStateOf(
        LoginUiState(
            "",
            "",
            true,
            LoginStatus.Normal,
            null
        )
    )
    val uiState: State<LoginUiState> = _uiState

    fun <T> onChange(field: LoginField, value: T) {
        if (field == LoginField.Username || field == LoginField.Password) {
            _uiState.value = _uiState.value.copy(errorText = null)
        }

        _uiState.value = when (field) {
            LoginField.Username -> _uiState.value.copy(username = value as String)
            LoginField.Password -> _uiState.value.copy(password = value as String)
            LoginField.Remember -> _uiState.value.copy(rememberMe = value as Boolean)
            LoginField.Error -> _uiState.value.copy(errorText = value as String?)
            LoginField.Status -> _uiState.value.copy(status = value as LoginStatus)
        }
    }

    fun login() {
        if (_uiState.value.status == LoginStatus.Trying) return

        onChange(LoginField.Error, null)

        val username = _uiState.value.username.trim()
        val password = _uiState.value.password.trim()

        if (username.isEmpty() || password.isEmpty()) return

        onChange(LoginField.Status, LoginStatus.Trying)

        viewModelScope.launch {
            val loginResult = authRepo.login(username, password)

            when (loginResult) {
                is ApiResult.Success<*> -> {
                    onChange(LoginField.Status, LoginStatus.Success)
                }

                is ApiResult.Error -> {
                    loginResult.appError.error?.let {
                        onChange(LoginField.Error, loginResult.appError.error)
                    }

                    onChange(LoginField.Status, LoginStatus.Normal)
                }

                is ApiResult.NetworkError -> {
                    onChange(
                        LoginField.Error,
                        "NetworkError" + loginResult.exception.message.let { ": $it" })

                    onChange(LoginField.Status, LoginStatus.Normal)
                }
            }
        }
    }
}

class LoginViewModelFactory(private val authRepo: AuthRepositoryImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}