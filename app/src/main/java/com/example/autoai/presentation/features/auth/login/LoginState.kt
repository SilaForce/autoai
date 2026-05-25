package com.example.autoai.presentation.features.auth.login

import androidx.compose.runtime.Stable

@Stable
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
)

