package com.example.autoai.presentation.features.splash

import androidx.lifecycle.viewModelScope
import com.example.autoai.base.BaseViewModel
import com.example.domain.model.app.StartDestination
import com.example.domain.datasource.PreferencesDataSource
import com.example.domain.usecase.session.CheckSessionUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class SplashViewModel(
    private val checkSessionUseCase: CheckSessionUseCase,
    private val preferencesDataSource: PreferencesDataSource,
) : BaseViewModel<SplashState, SplashEvent, SplashSideEffect>(
    SplashState()
) {

    private companion object {
        // If Firebase init or the session check takes longer than this, give up and
        // route to Auth. Better to over-route to login than to hang the splash forever.
        const val SESSION_RESOLVE_TIMEOUT_MS = 3_000L
    }

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
            val sessionDestination = try {
                withTimeout(SESSION_RESOLVE_TIMEOUT_MS) { checkSessionUseCase() }
            } catch (e: TimeoutCancellationException) {
                StartDestination.Auth
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                StartDestination.Auth
            }

            // If there's no session, fork on the onboarding flag: returning users go
            // straight to Login, first-time users see Onboarding.
            val startDestination = if (sessionDestination == StartDestination.Home) {
                StartDestination.Home
            } else {
                val onboarded = try {
                    preferencesDataSource.isOnboardingCompleted.first()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    false
                }
                if (onboarded) StartDestination.Login else StartDestination.Auth
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
