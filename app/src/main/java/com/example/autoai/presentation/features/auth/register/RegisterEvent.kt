package com.example.autoai.presentation.features.auth.register

import com.example.autoai.presentation.util.UiText

sealed interface RegisterEvent {
    data class OnNameChange(val name: String) : RegisterEvent
    data class OnEmailChange(val email: String) : RegisterEvent
    data class OnPasswordChange(val password: String) : RegisterEvent
    data class OnConfirmPasswordChange(val confirmPassword: String) : RegisterEvent
    data object OnRegisterClicked : RegisterEvent

    data object OnLoginClicked : RegisterEvent
}

sealed interface RegisterSideEffect {
    data class ShowError(val message: UiText) : RegisterSideEffect
    data class ShowSuccess(val message: UiText) : RegisterSideEffect
}