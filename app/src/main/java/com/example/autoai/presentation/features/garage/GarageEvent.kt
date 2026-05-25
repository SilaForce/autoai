package com.example.autoai.presentation.features.garage

import com.example.autoai.presentation.components.BottomNavItem

sealed interface GarageEvent {
    data object OnAddVehicleClicked : GarageEvent
    data object OnScreenResumed : GarageEvent
    data class OnVehicleSelected(val vehicleId: String) : GarageEvent
    data class OnNavItemSelected(val item: BottomNavItem) : GarageEvent
    data class OnVehicleLongPressed(val vehicleId: String) : GarageEvent
    data object OnDismissVehicleMenu : GarageEvent
    data class OnEditVehicleClicked(val vehicleId: String) : GarageEvent
    data class OnDeleteVehicleClicked(val vehicleId: String) : GarageEvent
    data object OnConfirmDeleteVehicle : GarageEvent
    data object OnDismissDeleteDialog : GarageEvent
    data object OnUndoDelete : GarageEvent
}
