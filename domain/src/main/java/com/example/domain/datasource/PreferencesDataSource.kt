package com.example.domain.datasource

import kotlinx.coroutines.flow.Flow

interface PreferencesDataSource {
    val isNotificationsEnabled: Flow<Boolean>
    val isDarkModeEnabled: Flow<Boolean>
    val isAiAutoRemindersEnabled: Flow<Boolean>
    val isOnboardingCompleted: Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setDarkModeEnabled(enabled: Boolean)
    suspend fun setAiAutoRemindersEnabled(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
}
