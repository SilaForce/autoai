package com.example.autoai.presentation.features.auth.login

import com.example.autoai.presentation.util.UiText

sealed interface LoginEvent {
    data class OnEmailChange(val email: String) : LoginEvent
    data class OnPasswordChange(val password: String) : LoginEvent
    data object OnLoginClicked : LoginEvent
    data object OnRegisterClicked : LoginEvent
    data object OnForgotPasswordClicked : LoginEvent
}

sealed interface LoginSideEffect {
    data class ShowError(val message: UiText) : LoginSideEffect
    data class ShowSuccess(val message: UiText) : LoginSideEffect
}
