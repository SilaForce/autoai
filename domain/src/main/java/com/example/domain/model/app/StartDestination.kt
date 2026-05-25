package com.example.domain.model.app

sealed interface StartDestination {
    // Fresh install / unfinished onboarding → routes to the Onboarding screen.
    data object Auth : StartDestination
    // Onboarding completed but no session → routes straight to Login.
    data object Login : StartDestination
    data object Setup : StartDestination
    data object Home : StartDestination
}

