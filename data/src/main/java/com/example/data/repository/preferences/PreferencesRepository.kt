package com.example.data.repository.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.repository.IPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository(
    private val context: Context
) : IPreferencesRepository {

    private object PreferencesKeys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val AI_AUTO_REMINDERS_ENABLED = booleanPreferencesKey("ai_auto_reminders_enabled")
    }

    override val isNotificationsEnabled: Flow<Boolean> =
        context.dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { it[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true }

    override val isDarkModeEnabled: Flow<Boolean> =
        context.dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { it[PreferencesKeys.DARK_MODE_ENABLED] ?: false }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    override suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DARK_MODE_ENABLED] = enabled }
    }

    override suspend fun isAiAutoRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AI_AUTO_REMINDERS_ENABLED] = enabled }
    }

    override val isAiAutoRemindersEnabled: Flow<Boolean>
        get() = context.dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { it[booleanPreferencesKey("ai_auto_reminders_enabled")] ?: false }
}
