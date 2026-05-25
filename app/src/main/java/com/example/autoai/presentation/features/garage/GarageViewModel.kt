package com.example.autoai.presentation.features.garage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.autoai.R
import com.example.autoai.base.BaseViewModel
import com.example.autoai.navigation.Route
import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.util.UiText
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.DeleteVehicleParams
import com.example.domain.usecase.vehicle.DeleteVehicleUseCase
import com.example.domain.usecase.vehicle.GetVehiclesParams
import com.example.domain.usecase.vehicle.GetVehiclesUseCase
import com.example.domain.usecase.vehicle.SetActiveVehicleParams
import com.example.domain.usecase.vehicle.SetActiveVehicleUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GarageViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val setActiveVehicleUseCase: SetActiveVehicleUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase,
) : BaseViewModel<GarageState, GarageEvent, GarageSideEffect>(
    GarageState(menuState = restoreMenuState(savedStateHandle))
) {

    private var currentUserId: String? = null
    private var hasResumedOnce = false

    private var pendingDeleteJob: Job? = null
    private var pendingDeleteContext: PendingDeleteContext? = null

    init {
        loadVehicles()
    }

    override fun onEvent(event: GarageEvent) {
        when (event) {
            GarageEvent.OnAddVehicleClicked -> emitSideEffect(GarageSideEffect.NavigateTo(Route.AddVehicle()))

            GarageEvent.OnScreenResumed -> {
                if (!hasResumedOnce) {
                    hasResumedOnce = true
                    return
                }
                refreshVehicles()
            }

            is GarageEvent.OnVehicleSelected -> {
                if (state.value.isUpdatingActiveVehicle) return
                if (state.value.vehicles.firstOrNull { it.id == event.vehicleId }?.isActive == true) return
                setActiveVehicle(event.vehicleId)
            }

            is GarageEvent.OnNavItemSelected -> handleBottomNavigation(event.item)

            is GarageEvent.OnVehicleLongPressed ->
                updateMenuState(VehicleMenuState.ShowingMenu(event.vehicleId))

            GarageEvent.OnDismissVehicleMenu -> updateMenuState(VehicleMenuState.Hidden)

            is GarageEvent.OnEditVehicleClicked -> {
                updateMenuState(VehicleMenuState.Hidden)
                emitSideEffect(GarageSideEffect.NavigateTo(Route.AddVehicle(vehicleId = event.vehicleId)))
            }

            is GarageEvent.OnDeleteVehicleClicked ->
                updateMenuState(VehicleMenuState.ConfirmingDelete(event.vehicleId))

            GarageEvent.OnDismissDeleteDialog -> updateMenuState(VehicleMenuState.Hidden)

            GarageEvent.OnConfirmDeleteVehicle -> deletePendingVehicle()

            GarageEvent.OnUndoDelete -> undoPendingDelete()
        }
    }

    private fun updateMenuState(newMenuState: VehicleMenuState) {
        persistMenuState(newMenuState)
        setState { it.copy(menuState = newMenuState) }
    }

    private fun persistMenuState(menuState: VehicleMenuState) {
        when (menuState) {
            VehicleMenuState.Hidden -> {
                savedStateHandle[KEY_MENU_TYPE] = null
                savedStateHandle[KEY_MENU_VEHICLE_ID] = null
            }
            is VehicleMenuState.ShowingMenu -> {
                savedStateHandle[KEY_MENU_TYPE] = MENU_TYPE_SHOWING
                savedStateHandle[KEY_MENU_VEHICLE_ID] = menuState.vehicleId
            }
            is VehicleMenuState.ConfirmingDelete -> {
                savedStateHandle[KEY_MENU_TYPE] = MENU_TYPE_CONFIRMING
                savedStateHandle[KEY_MENU_VEHICLE_ID] = menuState.vehicleId
            }
        }
    }

    private fun loadVehicles() {
        setState { it.copy(isLoading = true) }

        viewModelScope.launch {
            getCurrentUserUseCase(Unit)
                .onSuccess { user ->
                    currentUserId = user.id
                    getVehicles(user.id)
                }
                .onFailure { error ->
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(GarageSideEffect.ShowError(error.asUiText()))
                }
        }
    }

    private fun refreshVehicles() {
        val userId = currentUserId
        if (userId == null) {
            loadVehicles()
            return
        }

        viewModelScope.launch {
            getVehicles(userId)
        }
    }

    private suspend fun getVehicles(userId: String) {
        getVehiclesUseCase(GetVehiclesParams(userId))
            .onSuccess { vehicles ->
                setState {
                    it.copy(
                        isLoading = false,
                        isUpdatingActiveVehicle = false,
                        isDeleting = false,
                        vehicles = vehicles.map { vehicle -> vehicle.toGarageVehicleUi() },
                    )
                }
            }
            .onFailure { error ->
                setState {
                    it.copy(
                        isLoading = false,
                        isUpdatingActiveVehicle = false,
                        isDeleting = false,
                    )
                }
                emitSideEffect(GarageSideEffect.ShowError(error.asUiText()))
            }
    }

    private fun setActiveVehicle(vehicleId: String) {
        val userId = currentUserId ?: run {
            emitSideEffect(GarageSideEffect.ShowError(UiText.StringResource(R.string.error_unknown)))
            return
        }

        setState { it.copy(isUpdatingActiveVehicle = true) }

        viewModelScope.launch {
            setActiveVehicleUseCase(
                SetActiveVehicleParams(
                    userId = userId,
                    vehicleId = vehicleId,
                )
            ).onSuccess {
                refreshVehicles()
            }.onFailure { error ->
                setState { it.copy(isUpdatingActiveVehicle = false) }
                emitSideEffect(GarageSideEffect.ShowError(error.asUiText()))
            }
        }
    }

    private fun deletePendingVehicle() {
        val userId = currentUserId ?: return
        val snapshot = state.value
        val vehicleId = (snapshot.menuState as? VehicleMenuState.ConfirmingDelete)?.vehicleId ?: return
        val wasActive = snapshot.vehicles.any { it.id == vehicleId && it.isActive }
        val nextActiveId = snapshot.vehicles
            .firstOrNull { it.id != vehicleId && it.id != snapshot.pendingDeleteId }
            ?.id
            ?.takeIf { wasActive }

        // If there's already a pending delete for a different vehicle, commit it now (fire-and-forget).
        // The user clicking delete on a new vehicle implicitly accepts the prior one.
        val prior = pendingDeleteContext
        if (prior != null) {
            pendingDeleteJob?.cancel()
            viewModelScope.launch { executeCascadeDelete(prior) }
        }

        val ctx = PendingDeleteContext(vehicleId = vehicleId, userId = userId, nextActiveId = nextActiveId)
        pendingDeleteContext = ctx

        updateMenuState(VehicleMenuState.Hidden)
        setState { it.copy(pendingDeleteId = vehicleId) }
        emitSideEffect(GarageSideEffect.ShowUndoableDelete)

        pendingDeleteJob = viewModelScope.launch {
            delay(UNDO_WINDOW_MS)
            executeCascadeDelete(ctx)
            // Only clear if this is still the active pending; a newer one will have replaced it.
            if (pendingDeleteContext === ctx) {
                pendingDeleteContext = null
                pendingDeleteJob = null
            }
        }
    }

    private suspend fun executeCascadeDelete(ctx: PendingDeleteContext) {
        setState { it.copy(isDeleting = true) }
        deleteVehicleUseCase(DeleteVehicleParams(userId = ctx.userId, vehicleId = ctx.vehicleId))
            .onSuccess {
                if (ctx.nextActiveId != null) {
                    setState { it.copy(isUpdatingActiveVehicle = true) }
                    setActiveVehicleUseCase(
                        SetActiveVehicleParams(userId = ctx.userId, vehicleId = ctx.nextActiveId)
                    ).onFailure { error ->
                        setState { it.copy(isUpdatingActiveVehicle = false) }
                        emitSideEffect(GarageSideEffect.ShowError(error.asUiText()))
                    }
                }
                // Clear pending marker before refresh so the UI doesn't briefly re-show the deleted vehicle.
                setState { it.copy(pendingDeleteId = null) }
                refreshVehicles()
            }
            .onFailure { error ->
                setState { it.copy(pendingDeleteId = null, isDeleting = false) }
                emitSideEffect(GarageSideEffect.ShowError(error.asUiText()))
            }
    }

    private fun undoPendingDelete() {
        pendingDeleteJob?.cancel()
        pendingDeleteJob = null
        pendingDeleteContext = null
        setState { it.copy(pendingDeleteId = null) }
    }

    private fun handleBottomNavigation(item: BottomNavItem) {
        setState { it.copy(selectedNavItem = item) }
        val route: Route = when (item) {
            BottomNavItem.HOME -> Route.Home
            BottomNavItem.GARAGE -> return
            BottomNavItem.COSTS -> Route.Costs
            BottomNavItem.REMINDERS -> Route.Reminder
            BottomNavItem.AI_CHAT -> Route.AiChat
        }
        emitSideEffect(GarageSideEffect.NavigateTo(route))
    }

    private data class PendingDeleteContext(
        val vehicleId: String,
        val userId: String,
        val nextActiveId: String?,
    )

    private companion object {
        const val KEY_MENU_TYPE = "garage_menu_type"
        const val KEY_MENU_VEHICLE_ID = "garage_menu_vehicle_id"
        const val MENU_TYPE_SHOWING = "showing"
        const val MENU_TYPE_CONFIRMING = "confirming"
        const val UNDO_WINDOW_MS = 4_000L

        fun restoreMenuState(savedStateHandle: SavedStateHandle): VehicleMenuState {
            val type = savedStateHandle.get<String>(KEY_MENU_TYPE) ?: return VehicleMenuState.Hidden
            val vehicleId = savedStateHandle.get<String>(KEY_MENU_VEHICLE_ID) ?: return VehicleMenuState.Hidden
            return when (type) {
                MENU_TYPE_SHOWING -> VehicleMenuState.ShowingMenu(vehicleId)
                MENU_TYPE_CONFIRMING -> VehicleMenuState.ConfirmingDelete(vehicleId)
                else -> VehicleMenuState.Hidden
            }
        }
    }
}
