package com.example.autoai.presentation.features.garage.add

import androidx.compose.runtime.Stable
import com.example.domain.model.vehicle.FuelType

@Stable
data class AddVehicleState(
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val mileage: String = "",
    val licensePlate: String = "",
    val selectedFuelType: FuelType? = null,
    val fuelTypeOptions: List<FuelTypeOptionUi> = emptyList(),
    val isLoading: Boolean = false,
    val isFormDirty: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val showYearPicker: Boolean = false,
)
