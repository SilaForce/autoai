package com.example.autoai.presentation.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val navigator: IAppNavigator
) : ViewModel() { // Ovdje ne moramo imati kompleksan BaseViewModel jer nemamo stanje

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.OnSkipClicked,
            is OnboardingEvent.OnGetStartedClicked -> {
                completeOnboarding()
            }
        }
    }

    private fun completeOnboarding() {
        // Otvaramo coroutine jer će upis u DataStore biti suspend funkcija
        viewModelScope.launch {
            // 1. Ovdje će ići logika za snimanje u DataStore
            // completeOnboardingUseCase()

            // 2. Navigacija na Register (ili AuthGraph ako ga imaš)
            navigator.navigateTo(
                destination = Route.Register,
                popUpTo = Route.Onboarding,
                inclusive = true
            )
        }
    }
}