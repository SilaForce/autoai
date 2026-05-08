package com.example.autoai.presentation.features.garage.add

import com.example.autoai.R
import com.example.autoai.presentation.util.UiText
import com.example.domain.model.vehicle.FuelType

data class FuelTypeOptionUi(
    val fuelType: FuelType,
    val label: UiText,
    val isSelected: Boolean,
)

fun FuelType.toFuelTypeOptionUi(isSelected: Boolean): FuelTypeOptionUi {
    val label = when (this) {
        FuelType.BENZIN -> UiText.StringResource(R.string.add_vehicle_fuel_type_benzin)
        FuelType.DIZEL -> UiText.StringResource(R.string.add_vehicle_fuel_type_dizel)
        FuelType.PLIN -> UiText.StringResource(R.string.add_vehicle_fuel_type_plin)
        FuelType.ELEKTRO -> UiText.StringResource(R.string.add_vehicle_fuel_type_elektro)
        FuelType.HIBRID -> UiText.StringResource(R.string.add_vehicle_fuel_type_hibrid)
    }

    return FuelTypeOptionUi(
        fuelType = this,
        label = label,
        isSelected = isSelected,
    )
}
