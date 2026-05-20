package com.example.autoai.presentation.features.garage

import androidx.compose.runtime.Stable
import com.example.autoai.presentation.components.BottomNavItem

@Stable
data class GarageState(
    val vehicles: List<GarageVehicleUi> = emptyList(),
    val isLoading: Boolean = false,
    val isUpdatingActiveVehicle: Boolean = false,
    val selectedNavItem: BottomNavItem = BottomNavItem.GARAGE,
    val vehicleMenuId: String? = null,
    val pendingDeleteVehicleId: String? = null,
)
