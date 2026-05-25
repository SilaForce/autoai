package com.example.autoai.presentation.features.garage.add

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.example.domain.model.vehicle.FuelType

@Immutable
data class AddVehicleSnapshot(
    val make: String,
    val model: String,
    val year: String,
    val mileage: String,
    val licensePlate: String,
    val selectedFuelType: FuelType?,
)

@Stable
data class AddVehicleState(
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val mileage: String = "",
    val licensePlate: String = "",
    val selectedFuelType: FuelType? = null,
    val isLoading: Boolean = false,
    val isFormDirty: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val showYearPicker: Boolean = false,

    // Make autocomplete
    val allMakes: List<String> = emptyList(),
    val filteredMakes: List<String> = emptyList(),
    val isMakesDropdownExpanded: Boolean = false,
    val isMakesLoading: Boolean = false,

    // Model autocomplete
    val allModels: List<String> = emptyList(),
    val filteredModels: List<String> = emptyList(),
    val isModelsDropdownExpanded: Boolean = false,
    val isModelsLoading: Boolean = false,

    // Drives whether the Model field is enabled
    val isMakeSelected: Boolean = false,

    val isEditMode: Boolean = false,
    val originalIsActive: Boolean = false,
    // Captured field values at edit-load time. Null in add mode; non-null in edit mode.
    // calculateIsFormDirty compares current field values against this snapshot, so
    // simply opening an edit form and touching no fields leaves isFormDirty = false.
    val originalSnapshot: AddVehicleSnapshot? = null,
)
