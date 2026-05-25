package com.example.autoai.presentation.features.auth.register

import androidx.compose.runtime.Stable

@Stable
data class RegisterState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
)