package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface IPreferencesRepository {
    val isNotificationsEnabled: Flow<Boolean>
    val isDarkModeEnabled: Flow<Boolean>
    val isAiAutoRemindersEnabled: Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setDarkModeEnabled(enabled: Boolean)

    suspend fun setAiAutoRemindersEnabled(enabled: Boolean)
}
