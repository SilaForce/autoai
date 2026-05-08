package com.example.autoai.presentation.features.splash

sealed interface SplashEvent {
    data object ResolveStartDestination : SplashEvent
}
