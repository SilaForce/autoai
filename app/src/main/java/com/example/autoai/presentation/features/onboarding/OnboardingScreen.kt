package com.example.autoai.presentation.features.onboarding

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel()
) {
    // Pošto u ovom specifičnom slučaju Pager upravlja svojim stanjem,
    // ViewModelu prosljeđujemo samo referencu na onEvent funkciju.

    OnboardingContent(
        onEvent = viewModel::onEvent
    )
}