package com.promtuz.chat.presentation.state


sealed class LoginField {
    object Username : LoginField()
    object Password : LoginField()
    object Remember : LoginField()
    object Status : LoginField()
    object Error : LoginField()
}

sealed class LoginStatus(val text: String) {
    object Normal : LoginStatus("Continue")
    object Trying : LoginStatus("Please Wait...")
    object Success : LoginStatus("Success")
}

data class LoginUiState(
    val username: String,
    val password: String,
    val rememberMe: Boolean,
    val status: LoginStatus,
    val errorText: String?
)
