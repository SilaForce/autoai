package com.example.autoai.presentation.features.garage.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.autoai.R
import com.example.autoai.base.BaseViewModel
import com.example.autoai.presentation.util.UiText
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.model.vehicle.FuelType
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.AddVehicleParams
import com.example.domain.usecase.vehicle.AddVehicleUseCase
import kotlinx.coroutines.launch

class AddVehicleViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val addVehicleUseCase: AddVehicleUseCase,
) : BaseViewModel<AddVehicleState, AddVehicleEvent, AddVehicleSideEffect>(
    createInitialState(savedStateHandle)
) {

    override fun onEvent(event: AddVehicleEvent) {
        when (event) {
            is AddVehicleEvent.OnMakeChanged -> updateMake(event.value)
            is AddVehicleEvent.OnModelChanged -> updateModel(event.value)
            is AddVehicleEvent.OnFuelTypeSelected -> updateFuelType(event.fuelType)
            is AddVehicleEvent.OnMileageChanged -> updateMileage(event.value)
            is AddVehicleEvent.OnLicensePlateChanged -> updateLicensePlate(event.value)
            AddVehicleEvent.OnYearFieldClicked -> setState { it.copy(showYearPicker = true) }
            is AddVehicleEvent.OnYearSelected -> updateYear(event.year)
            AddVehicleEvent.OnYearPickerDismissed -> setState { it.copy(showYearPicker = false) }
            AddVehicleEvent.OnSaveClicked -> saveVehicle()
            AddVehicleEvent.OnBackClicked -> handleBackPress()
            AddVehicleEvent.OnDiscardDialogDismissed -> setState { it.copy(showDiscardDialog = false) }
            AddVehicleEvent.OnDiscardChangesConfirmed -> discardChanges()
        }
    }

    private fun updateMake(value: String) {
        savedStateHandle[KEY_MAKE] = value
        updateFormState { it.copy(make = value) }
    }

    private fun updateModel(value: String) {
        savedStateHandle[KEY_MODEL] = value
        updateFormState { it.copy(model = value) }
    }

    private fun updateYear(year: Int) {
        val value = year.toString()
        savedStateHandle[KEY_YEAR] = value
        updateFormState {
            it.copy(
                year = value,
                showYearPicker = false,
            )
        }
    }

    private fun updateFuelType(fuelType: FuelType) {
        savedStateHandle[KEY_FUEL_TYPE] = fuelType.name
        updateFormState {
            it.copy(
                selectedFuelType = fuelType,
                fuelTypeOptions = buildFuelTypeOptions(selectedFuelType = fuelType),
            )
        }
    }

    private fun updateMileage(value: String) {
        savedStateHandle[KEY_MILEAGE] = value
        updateFormState { it.copy(mileage = value) }
    }

    private fun updateLicensePlate(value: String) {
        savedStateHandle[KEY_LICENSE_PLATE] = value
        updateFormState { it.copy(licensePlate = value) }
    }

    private fun handleBackPress() {
        if (state.value.isFormDirty) {
            setState { it.copy(showDiscardDialog = true) }
        } else {
            emitSideEffect(AddVehicleSideEffect.NavigateBack)
        }
    }

    private fun discardChanges() {
        setState { it.copy(showDiscardDialog = false) }
        emitSideEffect(AddVehicleSideEffect.NavigateBack)
    }

    private fun saveVehicle() {
        val currentState = state.value
        val selectedFuelType = currentState.selectedFuelType

        val parsedYear = currentState.year.toIntOrNull()
        if (parsedYear == null) {
            emitSideEffect(
                AddVehicleSideEffect.ShowError(
                    UiText.StringResource(R.string.add_vehicle_error_invalid_year)
                )
            )
            return
        }

        val parsedMileage = when {
            currentState.mileage.isBlank() -> null
            else -> currentState.mileage.toIntOrNull()
        }

        if (currentState.mileage.isNotBlank() && parsedMileage == null) {
            emitSideEffect(
                AddVehicleSideEffect.ShowError(
                    UiText.StringResource(R.string.add_vehicle_error_invalid_mileage)
                )
            )
            return
        }

        if (selectedFuelType == null) {
            emitSideEffect(
                AddVehicleSideEffect.ShowError(
                    UiText.StringResource(R.string.add_vehicle_error_select_fuel_type)
                )
            )
            return
        }

        setState { it.copy(isLoading = true) }

        viewModelScope.launch {
            getCurrentUserUseCase(Unit)
                .onSuccess { user ->
                    addVehicleUseCase(
                        AddVehicleParams(
                            userId = user.id,
                            make = currentState.make,
                            model = currentState.model,
                            year = parsedYear,
                            fuelType = selectedFuelType,
                            mileage = parsedMileage,
                            licensePlate = currentState.licensePlate,
                        )
                    ).onSuccess {
                        setState { it.copy(isLoading = false, isFormDirty = false) }
                        emitSideEffect(AddVehicleSideEffect.NavigateBack)
                    }.onFailure { error ->
                        setState { it.copy(isLoading = false) }
                        emitSideEffect(AddVehicleSideEffect.ShowError(error.asUiText()))
                    }
                }
                .onFailure { error ->
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(AddVehicleSideEffect.ShowError(error.asUiText()))
                }
        }
    }

    private companion object {
        const val KEY_MAKE = "add_vehicle_make"
        const val KEY_MODEL = "add_vehicle_model"
        const val KEY_YEAR = "add_vehicle_year"
        const val KEY_FUEL_TYPE = "add_vehicle_fuel_type"
        const val KEY_MILEAGE = "add_vehicle_mileage"
        const val KEY_LICENSE_PLATE = "add_vehicle_license_plate"

        fun createInitialState(savedStateHandle: SavedStateHandle): AddVehicleState {
            val selectedFuelType = savedStateHandle.get<String>(KEY_FUEL_TYPE)
                ?.let { fuelTypeName -> runCatching { FuelType.valueOf(fuelTypeName) }.getOrNull() }

            val initialState = AddVehicleState(
                make = savedStateHandle[KEY_MAKE] ?: "",
                model = savedStateHandle[KEY_MODEL] ?: "",
                year = savedStateHandle[KEY_YEAR] ?: "",
                mileage = savedStateHandle[KEY_MILEAGE] ?: "",
                licensePlate = savedStateHandle[KEY_LICENSE_PLATE] ?: "",
                selectedFuelType = selectedFuelType,
                fuelTypeOptions = buildFuelTypeOptions(selectedFuelType),
            )

            return initialState.copy(isFormDirty = calculateIsFormDirty(initialState))
        }

        fun buildFuelTypeOptions(selectedFuelType: FuelType?): List<FuelTypeOptionUi> {
            return FuelType.entries.map { fuelType ->
                fuelType.toFuelTypeOptionUi(isSelected = fuelType == selectedFuelType)
            }
        }

        fun calculateIsFormDirty(state: AddVehicleState): Boolean {
            return state.make.isNotBlank() ||
                state.model.isNotBlank() ||
                state.year.isNotBlank() ||
                state.mileage.isNotBlank() ||
                state.licensePlate.isNotBlank() ||
                state.selectedFuelType != null
        }
    }

    private fun updateFormState(transform: (AddVehicleState) -> AddVehicleState) {
        setState { currentState ->
            val updatedState = transform(currentState)
            updatedState.copy(isFormDirty = calculateIsFormDirty(updatedState))
        }
    }
}
