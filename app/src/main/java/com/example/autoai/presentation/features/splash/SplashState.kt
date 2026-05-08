package com.example.autoai.presentation.features.splash

import com.example.domain.model.app.StartDestination

data class SplashState(
    val isLoading: Boolean = true,
    val startDestination: StartDestination? = null,
)
