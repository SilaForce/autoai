package com.example.autoai.presentation.features.garage.add

import com.example.autoai.presentation.util.UiText

sealed interface AddVehicleSideEffect {
    data class ShowError(val message: UiText) : AddVehicleSideEffect
    data object NavigateBack : AddVehicleSideEffect
}
