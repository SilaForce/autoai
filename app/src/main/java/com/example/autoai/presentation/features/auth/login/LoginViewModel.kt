package com.example.autoai.presentation.features.auth.login

import androidx.lifecycle.viewModelScope
import com.example.autoai.base.BaseViewModel
import com.example.autoai.localization.AppStrings
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import com.example.autoai.presentation.util.UiText
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.usecase.login.LoginParams
import com.example.domain.usecase.login.LoginUseCase
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val navigator: IAppNavigator,
) : BaseViewModel<LoginState, LoginEvent, LoginSideEffect>(
    LoginState()
) {
    override fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnEmailChange -> setState { it.copy(email = event.email) }
            is LoginEvent.OnPasswordChange -> setState { it.copy(password = event.password) }
            is LoginEvent.OnLoginClicked -> withDebounce { login() }
            is LoginEvent.OnRegisterClicked -> navigateToRegister()
            is LoginEvent.OnForgotPasswordClicked -> { /* TODO */ }
        }
    }

    private fun login() {
        val currentState = state.value

        setState { it.copy(isLoading = true) }

        viewModelScope.launch {
            loginUseCase(LoginParams(currentState.email, currentState.password))
                .onSuccess {
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(
                        LoginSideEffect.ShowSuccess(
                            UiText.StringResource(AppStrings.Auth.loginSuccessRes)
                        )
                    )
                    // Navigate immediately; the snackbar paints briefly during the screen
                    // transition. Previously this blocked for 1500ms which would fire nav
                    // from a non-foreground coroutine if the user backgrounded the app.
                    navigator.navigateTo(
                        destination = Route.Home,
                        popUpTo = Route.AuthGraph,
                        inclusive = true,
                    )
                }
                .onFailure { error ->
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(LoginSideEffect.ShowError(error.asUiText()))
                }
        }
    }

    private fun navigateToRegister() {
        navigator.navigateTo(
            destination = Route.Register,
            popUpTo = Route.Login,
            inclusive = true
        )
    }
}
