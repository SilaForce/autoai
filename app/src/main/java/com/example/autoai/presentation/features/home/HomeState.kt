package com.example.autoai.presentation.features.home

import com.example.autoai.presentation.components.BottomNavItem

data class HomeState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val hasActiveVehicle: Boolean = false,
    val activeVehicleName: String = "",
    val activeVehiclePlate: String = "",
    // Formatted total expenses for the active vehicle, e.g. "1.045". Em-dash ("—") is
    // used as the placeholder for "no costs logged yet" so it's distinguishable from
    // a legitimate "0" (user logged $0).
    val totalExpenses: String = "—",
    val dueReminderTitle: String = "",
    val dueReminderDate: String = "",
    val currency: String = "BAM",
    val selectedNavItem: BottomNavItem = BottomNavItem.HOME,
)
