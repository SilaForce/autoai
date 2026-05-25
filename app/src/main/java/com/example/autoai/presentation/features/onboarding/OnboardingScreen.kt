package com.example.autoai.presentation.features.onboarding

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel()
) {
    // The Pager owns its own state, so the VM only receives the onEvent callback.
    OnboardingContent(
        onEvent = viewModel::onEvent
    )
}