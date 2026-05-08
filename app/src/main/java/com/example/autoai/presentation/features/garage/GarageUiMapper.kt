package com.example.autoai.presentation.features.garage

import com.example.autoai.R
import com.example.autoai.presentation.util.UiText
import com.example.domain.model.vehicle.Vehicle

fun Vehicle.toGarageVehicleUi(): GarageVehicleUi {
    return GarageVehicleUi(
        id = id,
        title = UiText.DynamicString("$make $model"),
        subtitle = licensePlate
            ?.takeIf { it.isNotBlank() }
            ?.let(UiText::DynamicString)
            ?: UiText.StringResource(R.string.garage_no_license_plate),
        isActive = isActive,
    )
}
