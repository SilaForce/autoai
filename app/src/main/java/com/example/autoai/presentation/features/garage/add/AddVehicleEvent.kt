package com.example.autoai.presentation.features.garage.add

import com.example.domain.model.vehicle.FuelType

sealed interface AddVehicleEvent {
    data class OnMakeChanged(val value: String) : AddVehicleEvent
    data class OnModelChanged(val value: String) : AddVehicleEvent
    data class OnFuelTypeSelected(val fuelType: FuelType) : AddVehicleEvent
    data class OnMileageChanged(val value: String) : AddVehicleEvent
    data class OnLicensePlateChanged(val value: String) : AddVehicleEvent
    data object OnYearFieldClicked : AddVehicleEvent
    data class OnYearSelected(val year: Int) : AddVehicleEvent
    data object OnYearPickerDismissed : AddVehicleEvent
    data object OnSaveClicked : AddVehicleEvent
    data object OnBackClicked : AddVehicleEvent
    data object OnDiscardDialogDismissed : AddVehicleEvent
    data object OnDiscardChangesConfirmed : AddVehicleEvent
    data class OnMakeDropdownExpandedChange(val expanded: Boolean) : AddVehicleEvent
    data class OnMakeSelected(val make: String) : AddVehicleEvent
    data class OnModelDropdownExpandedChange(val expanded: Boolean) : AddVehicleEvent
    data class OnModelSelected(val model: String) : AddVehicleEvent
}
