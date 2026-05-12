package com.example.autoai.presentation.features.profile

import com.example.autoai.presentation.util.UiText

sealed interface ProfileEvent {
    data object OnEditProfileClick : ProfileEvent
    data object OnRetry : ProfileEvent
}

sealed interface ProfileSideEffect {
    data class ShowError(val message: UiText) : ProfileSideEffect
}
