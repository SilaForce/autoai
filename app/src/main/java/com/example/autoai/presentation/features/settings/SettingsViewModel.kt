package com.example.autoai.presentation.features.settings

import androidx.lifecycle.viewModelScope
import com.example.autoai.R
import com.example.autoai.base.BaseViewModel
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import com.example.autoai.presentation.util.UiText
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.model.user.User
import com.example.domain.datasource.PreferencesDataSource
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.user.LogoutUseCase
import com.example.domain.usecase.user.UpdateUserParams
import com.example.domain.usecase.user.UpdateUserUseCase
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val navigator: IAppNavigator,
    private val preferencesDataSource: PreferencesDataSource
): BaseViewModel<SettingsState, SettingsEvent, SettingsSideEffect>(SettingsState()) {

    private var currentUser: User? = null

    init {
        observePreferences()
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            getCurrentUserUseCase(Unit).onSuccess { user ->
                currentUser = user
                setState { it.copy(currency = user.currency) }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesDataSource.isDarkModeEnabled.collect { enabled ->
                setState { it.copy(isDarkModeEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferencesDataSource.isNotificationsEnabled.collect { enabled ->
                setState { it.copy(notificationsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferencesDataSource.isAiAutoRemindersEnabled.collect { enabled ->
                setState { it.copy(aiAutoRemindersEnabled = enabled) }
            }
        }
    }

    override fun onEvent(event: SettingsEvent) {
        when(event){
            is SettingsEvent.OnToggleDarkMode -> {
                viewModelScope.launch {
                    preferencesDataSource.setDarkModeEnabled(event.isEnabled)
                }
            }

            is SettingsEvent.OnToggleNotifications -> {
                viewModelScope.launch {
                    preferencesDataSource.setNotificationsEnabled(event.isEnabled)
                }
            }

            is SettingsEvent.OnToggleAiAutoReminders -> {
                viewModelScope.launch {
                   preferencesDataSource.setAiAutoRemindersEnabled(event.isEnabled)
                }
            }

            is SettingsEvent.OnBackClicked -> navigateBack()

            is SettingsEvent.OnChangeLanguageClicked,
            is SettingsEvent.OnPrivacyPolicyClicked -> {
                emitSideEffect(
                    SettingsSideEffect.ShowError(
                        UiText.StringResource(R.string.settings_coming_soon)
                    )
                )
            }

            SettingsEvent.OnChangeCurrencyClicked -> {
                setState { it.copy(isCurrencyDialogOpen = true) }
            }

            SettingsEvent.OnDismissCurrencyDialog -> {
                setState { it.copy(isCurrencyDialogOpen = false) }
            }

            is SettingsEvent.OnCurrencySelected -> selectCurrency(event.code)

            is SettingsEvent.OnLogOutClicked -> logout()
        }
    }

    private fun selectCurrency(code: String) {
        val user = currentUser ?: return
        if (code == user.currency) {
            setState { it.copy(isCurrencyDialogOpen = false) }
            return
        }

        setState { it.copy(isCurrencyDialogOpen = false, isUpdatingCurrency = true) }

        viewModelScope.launch {
            updateUserUseCase(
                UpdateUserParams(
                    name = user.name,
                    username = user.username,
                    phoneNumber = user.phoneNumber,
                    currency = code,
                )
            )
                .onSuccess { updated ->
                    currentUser = updated
                    setState { it.copy(currency = updated.currency, isUpdatingCurrency = false) }
                    emitSideEffect(
                        SettingsSideEffect.ShowMessage(
                            UiText.StringResource(R.string.settings_currency_updated)
                        )
                    )
                }
                .onFailure { error ->
                    setState { it.copy(isUpdatingCurrency = false) }
                    emitSideEffect(SettingsSideEffect.ShowError(error.asUiText()))
                }
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
                        popUpTo = Route.Home,
                        inclusive = true,
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