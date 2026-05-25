package com.example.autoai.presentation.features.settings

data class SettingsState (
    val isLoading: Boolean = false,
    val isDarkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val aiAutoRemindersEnabled: Boolean = false,
    val currency: String = "BAM",
    val isCurrencyDialogOpen: Boolean = false,
    val isUpdatingCurrency: Boolean = false,
)