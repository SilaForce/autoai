package com.example.autoai.presentation.features.settings

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import com.example.autoai.base.BaseViewModel
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.repository.IPreferencesRepository
import com.example.domain.usecase.user.LogoutUseCase
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val navigator: IAppNavigator,
    private val preferencesRepository: IPreferencesRepository
): BaseViewModel<SettingsState, SettingsEvent, SettingsSideEffect>(SettingsState()) {

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.isDarkModeEnabled.collect { enabled ->
                setState { it.copy(isDarkModeEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.isNotificationsEnabled.collect { enabled ->
                setState { it.copy(notificationsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.isAiAutoRemindersEnabled.collect { enabled ->
                setState { it.copy(aiAutoRemindersEnabled = enabled) }
            }
        }
    }

    override fun onEvent(event: SettingsEvent) {
        when(event){
            is SettingsEvent.OnToggleDarkMode -> {
                viewModelScope.launch {
                    preferencesRepository.setDarkModeEnabled(event.isEnabled)
                }
            }

            is SettingsEvent.OnToggleNotifications -> {
                viewModelScope.launch {
                    preferencesRepository.setNotificationsEnabled(event.isEnabled)
                }
            }

            is SettingsEvent.OnToggleAiAutoReminders -> {
                viewModelScope.launch {
                   preferencesRepository.setAiAutoRemindersEnabled(event.isEnabled)
                }
            }

            is SettingsEvent.OnBackClicked -> navigateBack()



            is SettingsEvent.OnChangeLanguageClicked -> {
                // Handle language change logic here
            }

            is SettingsEvent.OnPrivacyPolicyClicked -> {
                // Handle privacy policy navigation here
            }

            is SettingsEvent.OnLogOutClicked -> logout()

        }
    }

    private fun logout(){
        setState { it.copy(isLoading = true) }

        viewModelScope.launch {
            logoutUseCase(Unit)
                .onSuccess {
                    setState { it.copy(isLoading = false) }
                    navigator.navigateTo(
                        destination = Route.AuthGraph,
                        popUpTo = 0,
                        inclusive = true
                    )
                 }
                .onFailure { error ->
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(SettingsSideEffect.ShowError(error.asUiText()))
                }
        }

    }

    private fun navigateBack() {
        navigator.navigateBack()
    }

}