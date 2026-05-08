package com.example.autoai.presentation.features.onboarding

sealed interface OnboardingEvent {
    data object OnSkipClicked : OnboardingEvent
    data object OnGetStartedClicked : OnboardingEvent
}
