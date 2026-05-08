package com.example.autoai.presentation.features.auth.register

data class RegisterState (
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
)