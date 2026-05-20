package com.example.autoai.presentation.features.garage.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.autoai.R
import com.example.autoai.base.BaseViewModel
import com.example.autoai.navigation.Route
import com.example.autoai.presentation.util.UiText
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.model.vehicle.FuelType
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.AddVehicleParams
import com.example.domain.usecase.vehicle.AddVehicleUseCase
import com.example.domain.usecase.vehicle.GetCarMakesUseCase
import com.example.domain.usecase.vehicle.GetModelsForMakeUseCase
import com.example.domain.usecase.vehicle.GetVehicleByIdUseCase
import com.example.domain.usecase.vehicle.UpdateVehicleParams
import com.example.domain.usecase.vehicle.UpdateVehicleUseCase
import kotlinx.coroutines.launch

class AddVehicleViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val addVehicleUseCase: AddVehicleUseCase,
    private val updateVehicleUseCase: UpdateVehicleUseCase,
    private val getVehicleByIdUseCase: GetVehicleByIdUseCase,
    private val getCarMakesUseCase: GetCarMakesUseCase,
    private val getModelsForMakeUseCase: GetModelsForMakeUseCase,
) : BaseViewModel<AddVehicleState, AddVehicleEvent, AddVehicleSideEffect>(
    createInitialState(savedStateHandle)
) {

    private val vehicleId: String? = savedStateHandle.toRoute<Route.AddVehicle>().vehicleId

    init {
        loadMakes()
        if (vehicleId != null) {
            loadVehicleForEdit(vehicleId)
        }
    }

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
            is AddVehicleEvent.OnMakeDropdownExpandedChange ->
                setState { it.copy(isMakesDropdownExpanded = event.expanded) }
            is AddVehicleEvent.OnMakeSelected -> handleMakeSelected(event.make)
            is AddVehicleEvent.OnModelDropdownExpandedChange ->
                setState { it.copy(isModelsDropdownExpanded = event.expanded) }
            is AddVehicleEvent.OnModelSelected -> handleModelSelected(event.model)
        }
    }

    private fun loadVehicleForEdit(id: String) {
        setState { it.copy(isLoading = true) }
        viewModelScope.launch {
            getVehicleByIdUseCase(id)
                .onSuccess { vehicle ->
                    val fuelType = vehicle.fuelType
                    setState { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isEditMode = true,
                            originalIsActive = vehicle.isActive,
                            make = vehicle.make,
                            model = vehicle.model,
                            year = vehicle.year.toString(),
                            mileage = vehicle.mileage?.toString() ?: "",
                            licensePlate = vehicle.licensePlate ?: "",
                            selectedFuelType = fuelType,
                            fuelTypeOptions = buildFuelTypeOptions(fuelType),
                            isMakeSelected = true,
                            isFormDirty = false,
                        )
                    }
                    loadModelsForMake(vehicle.make)
                }
                .onFailure { error ->
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(AddVehicleSideEffect.ShowError(error.asUiText()))
                }
        }
    }

    private fun loadMakes() {
        setState { it.copy(isMakesLoading = true) }
        viewModelScope.launch {
            getCarMakesUseCase(Unit)
                .onSuccess { makes ->
                    setState { currentState ->
                        currentState.copy(
                            isMakesLoading = false,
                            allMakes = makes,
                            filteredMakes = makes
                        )
                    }
                }
                .onFailure { error ->
                    setState { it.copy(isMakesLoading = false) }
                    emitSideEffect(AddVehicleSideEffect.ShowError(error.asUiText()))
                }
        }
    }

    private fun loadModelsForMake(make: String) {
        setState { it.copy(isModelsLoading = true, allModels = emptyList(), filteredModels = emptyList()) }
        viewModelScope.launch {
            getModelsForMakeUseCase(make)
                .onSuccess { models ->
                    setState { currentState ->
                        currentState.copy(
                            isModelsLoading = false,
                            allModels = models,
                            filteredModels = models
                        )
                    }
                }
                .onFailure { error ->
                    setState { it.copy(isModelsLoading = false) }
                    emitSideEffect(AddVehicleSideEffect.ShowError(error.asUiText()))
                }
        }
    }

    private fun updateMake(value: String) {
        savedStateHandle[KEY_MAKE] = value
        updateFormState { current ->
            val filtered = current.allMakes.filter { it.contains(value, ignoreCase = true) }
            val stillMatches = current.allMakes.any { it.equals(value, ignoreCase = true) }
            current.copy(
                make = value,
                filteredMakes = filtered,
                isMakeSelected = stillMatches,
                // Auto-open dropdown while typing if there are matches
                isMakesDropdownExpanded = value.isNotBlank() && filtered.isNotEmpty(),
                // If make no longer exactly matches, clear model + models
                model = if (stillMatches) current.model else "",
                allModels = if (stillMatches) current.allModels else emptyList(),
                filteredModels = if (stillMatches) current.filteredModels else emptyList(),
            )
        }
    }

    private fun handleMakeSelected(make: String) {
        savedStateHandle[KEY_MAKE] = make
        updateFormState {
            it.copy(
                make = make,
                isMakesDropdownExpanded = false,
                isMakeSelected = true,
                model = "",  // Reset model when make changes
            )
        }
        loadModelsForMake(make)
    }

    private fun updateModel(value: String) {
        savedStateHandle[KEY_MODEL] = value
        updateFormState { current ->
            val filtered = current.allModels.filter { it.contains(value, ignoreCase = true) }
            current.copy(
                model = value,
                filteredModels = filtered,
                isModelsDropdownExpanded = value.isNotBlank() && filtered.isNotEmpty(),
            )
        }
    }

    private fun handleModelSelected(model: String) {
        savedStateHandle[KEY_MODEL] = model
        updateFormState {
            it.copy(model = model, isModelsDropdownExpanded = false)
        }
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
                    if (vehicleId != null) {
                        updateVehicleUseCase(
                            UpdateVehicleParams(
                                vehicleId = vehicleId,
                                userId = user.id,
                                make = currentState.make,
                                model = currentState.model,
                                year = parsedYear,
                                fuelType = selectedFuelType,
                                mileage = parsedMileage,
                                licensePlate = currentState.licensePlate,
                                isActive = currentState.originalIsActive,
                            )
                        ).onSuccess {
                            setState { it.copy(isLoading = false, isFormDirty = false) }
                            emitSideEffect(AddVehicleSideEffect.NavigateBack)
                        }.onFailure { error ->
                            setState { it.copy(isLoading = false) }
                            emitSideEffect(AddVehicleSideEffect.ShowError(error.asUiText()))
                        }
                    } else {
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
