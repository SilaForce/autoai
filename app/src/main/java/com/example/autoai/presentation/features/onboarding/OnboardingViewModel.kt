package com.example.autoai.presentation.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import com.example.domain.datasource.PreferencesDataSource
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val navigator: IAppNavigator,
    private val preferencesDataSource: PreferencesDataSource,
) : ViewModel() {

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.OnSkipClicked,
            is OnboardingEvent.OnGetStartedClicked -> {
                completeOnboarding()
            }
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            // Persist completion so the next cold-start / sign-out routes to Login,
            // not back through Onboarding.
            preferencesDataSource.setOnboardingCompleted(true)

            navigator.navigateTo(
                destination = Route.Register,
                popUpTo = Route.Onboarding,
                inclusive = true,
            )
        }
    }
}