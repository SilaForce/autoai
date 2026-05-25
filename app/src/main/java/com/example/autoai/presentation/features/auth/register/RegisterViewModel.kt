package com.example.autoai.presentation.features.auth.register

import androidx.lifecycle.viewModelScope
import com.example.autoai.localization.AppStrings
import com.example.autoai.base.BaseViewModel
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import com.example.autoai.presentation.util.UiText
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.usecase.register.RegisterParams
import com.example.domain.usecase.register.RegisterUseCase
import kotlinx.coroutines.launch

class RegisterViewModel (
    private val registerUseCase: RegisterUseCase,
    private val navigator: IAppNavigator,
): BaseViewModel <RegisterState, RegisterEvent, RegisterSideEffect>(
    RegisterState()
){
    override fun onEvent(event: RegisterEvent) {
        when(event){
            is RegisterEvent.OnNameChange -> setState { it.copy(name = event.name) }
            is RegisterEvent.OnEmailChange -> setState { it.copy(email = event.email) }
            is RegisterEvent.OnPasswordChange -> setState { it.copy(password = event.password) }
            is RegisterEvent.OnConfirmPasswordChange -> setState { it.copy(confirmPassword = event.confirmPassword) }
            is RegisterEvent.OnRegisterClicked -> withDebounce { register() }
            is RegisterEvent.OnLoginClicked -> navigateToLogin()
        }
    }

    private fun register() {
        val currentState = state.value

        if (currentState.password != currentState.confirmPassword) {
            emitSideEffect(RegisterSideEffect.ShowError(UiText.StringResource(AppStrings.Auth.passwordsDoNotMatchRes)))
            return
        }

        setState { it.copy(isLoading = true) }

        viewModelScope.launch {
            registerUseCase(
                RegisterParams(currentState.name, currentState.email, currentState.password)
            ).onSuccess {
                setState { it.copy(isLoading = false) }
                emitSideEffect(RegisterSideEffect.ShowSuccess(
                    UiText.StringResource(AppStrings.Auth.registerSuccessRes)
                ))
                // Navigate immediately; snackbar paints briefly during the screen transition.
                navigateToLogin()
            }.onFailure { error ->
                setState { it.copy(isLoading = false) }
                emitSideEffect(RegisterSideEffect.ShowError(error.asUiText()))
            }
        }
    }

    private fun navigateToLogin() {
        navigator.navigateTo(
            destination = Route.Login,
            popUpTo = Route.Register,
            inclusive = true
        )
    }
}