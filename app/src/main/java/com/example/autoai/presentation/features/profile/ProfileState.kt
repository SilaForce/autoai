package com.example.autoai.presentation.features.profile

data class ProfileState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val userEmail: String = "",
    val userInitial: String = "",
    val memberSince: String = "",
    val plan: String = "",
    val vehicleCount: Int = 0,
    val totalCostCount: Int = 0,
    val username: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl : String? = null
)
