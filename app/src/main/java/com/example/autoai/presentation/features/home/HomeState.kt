package com.example.autoai.presentation.features.home

import com.example.autoai.presentation.components.BottomNavItem

data class HomeState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val hasActiveVehicle: Boolean = false,
    val activeVehicleName: String = "",
    val activeVehiclePlate: String = "",
    // Formatted total expenses for the active vehicle, e.g. "1.045"
    val totalExpenses: String = "0",
    val selectedNavItem: BottomNavItem = BottomNavItem.HOME,
)
