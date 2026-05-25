package com.example.autoai.presentation.features.settings

sealed interface SettingsEvent {
    data class OnToggleDarkMode(val isEnabled: Boolean) : SettingsEvent
    data class OnToggleNotifications(val isEnabled: Boolean) : SettingsEvent
    data class OnToggleAiAutoReminders(val isEnabled: Boolean) : SettingsEvent
    data object OnChangeLanguageClicked : SettingsEvent
    data object OnChangeCurrencyClicked : SettingsEvent
    data object OnDismissCurrencyDialog : SettingsEvent
    data class OnCurrencySelected(val code: String) : SettingsEvent
    data object OnPrivacyPolicyClicked : SettingsEvent
    data object OnLogOutClicked : SettingsEvent
    data object OnBackClicked : SettingsEvent
}