package com.example.autoai.presentation.features.settings

data class SettingsState (
    val isLoading: Boolean = false,
    val isDarkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
)