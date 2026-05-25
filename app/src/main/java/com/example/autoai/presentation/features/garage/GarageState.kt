package com.example.autoai.presentation.features.garage

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.example.autoai.presentation.components.BottomNavItem

@Immutable
sealed interface VehicleMenuState {
    data object Hidden : VehicleMenuState
    data class ShowingMenu(val vehicleId: String) : VehicleMenuState
    data class ConfirmingDelete(val vehicleId: String) : VehicleMenuState
}

@Stable
data class GarageState(
    val vehicles: List<GarageVehicleUi> = emptyList(),
    val isLoading: Boolean = false,
    val isUpdatingActiveVehicle: Boolean = false,
    val isDeleting: Boolean = false,
    val selectedNavItem: BottomNavItem = BottomNavItem.GARAGE,
    val menuState: VehicleMenuState = VehicleMenuState.Hidden,
    // Vehicle id that the user has confirmed deletion for but hasn't passed the
    // undo window yet. Hidden from the visible list; the cascade fires after a
    // timeout unless the user taps Undo.
    val pendingDeleteId: String? = null,
) {
    val isMutating: Boolean get() = isUpdatingActiveVehicle || isDeleting
    val visibleVehicles: List<GarageVehicleUi>
        get() = if (pendingDeleteId == null) vehicles else vehicles.filter { it.id != pendingDeleteId }
}
