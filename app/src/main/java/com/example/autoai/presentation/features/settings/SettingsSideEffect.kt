package com.example.autoai.presentation.features.settings

import com.example.autoai.presentation.util.UiText

sealed interface SettingsSideEffect {
    data class ShowError(val message: UiText) : SettingsSideEffect
    data class ShowMessage(val message: UiText) : SettingsSideEffect
}