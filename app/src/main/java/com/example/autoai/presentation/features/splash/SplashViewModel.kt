package com.example.autoai.presentation.features.splash

import androidx.lifecycle.viewModelScope
import com.example.autoai.base.BaseViewModel
import com.example.domain.model.app.StartDestination
import com.example.domain.usecase.session.CheckSessionUseCase
import kotlinx.coroutines.launch

class SplashViewModel(
    private val checkSessionUseCase: CheckSessionUseCase,
) : BaseViewModel<SplashState, SplashEvent, SplashSideEffect>(
    SplashState()
) {

    init {
        onEvent(SplashEvent.ResolveStartDestination)
    }

    override fun onEvent(event: SplashEvent) {
        when (event) {
            SplashEvent.ResolveStartDestination -> resolveStartDestination()
        }
    }

    private fun resolveStartDestination() {
        viewModelScope.launch {
            val startDestination = runCatching {
                checkSessionUseCase()
            }.getOrElse {
                StartDestination.Auth
            }

            setState {
                it.copy(
                    isLoading = false,
                    startDestination = startDestination,
                )
            }
        }
    }
}
