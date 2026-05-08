package com.example.autoai.presentation.features.garage

import com.example.autoai.presentation.components.BottomNavItem

sealed interface GarageEvent {
    data object OnAddVehicleClicked : GarageEvent
    data object OnScreenResumed : GarageEvent
    data class OnVehicleSelected(val vehicleId: String) : GarageEvent
    data class OnNavItemSelected(val item: BottomNavItem) : GarageEvent
}
