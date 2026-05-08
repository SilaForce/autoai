package com.example.autoai.presentation.features.garage

import androidx.lifecycle.viewModelScope
import com.example.autoai.R
import com.example.autoai.base.BaseViewModel
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.util.UiText
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.GetVehiclesParams
import com.example.domain.usecase.vehicle.GetVehiclesUseCase
import com.example.domain.usecase.vehicle.SetActiveVehicleParams
import com.example.domain.usecase.vehicle.SetActiveVehicleUseCase
import kotlinx.coroutines.launch

class GarageViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val setActiveVehicleUseCase: SetActiveVehicleUseCase,
    private val navigator: IAppNavigator,
) : BaseViewModel<GarageState, GarageEvent, GarageSideEffect>(GarageState()) {

    private var currentUserId: String? = null

    init {
        loadVehicles()
    }

    override fun onEvent(event: GarageEvent) {
        when (event) {
            GarageEvent.OnAddVehicleClicked -> navigator.navigateTo(Route.AddVehicle)

            GarageEvent.OnScreenResumed -> {
                if (!state.value.isLoading && !state.value.isUpdatingActiveVehicle) {
                    refreshVehicles()
                }
            }

            is GarageEvent.OnVehicleSelected -> {
                if (state.value.isUpdatingActiveVehicle) return
                if (state.value.vehicles.firstOrNull { it.id == event.vehicleId }?.isActive == true) return
                setActiveVehicle(event.vehicleId)
            }

            is GarageEvent.OnNavItemSelected -> handleBottomNavigation(event.item)
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
                        vehicles = vehicles.map { vehicle -> vehicle.toGarageVehicleUi() },
                    )
                }
            }
            .onFailure { error ->
                setState {
                    it.copy(
                        isLoading = false,
                        isUpdatingActiveVehicle = false,
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

    private fun handleBottomNavigation(item: BottomNavItem) {
        when (item) {
            BottomNavItem.HOME -> {
                setState { it.copy(selectedNavItem = BottomNavItem.HOME) }
                 navigator.navigateTo(Route.Home)
            }

            BottomNavItem.GARAGE -> setState { it.copy(selectedNavItem = BottomNavItem.GARAGE) }
            BottomNavItem.COSTS -> {
                setState { it.copy(selectedNavItem = BottomNavItem.COSTS) }
                navigator.navigateTo(Route.Costs)
            }
            BottomNavItem.REMINDERS -> {
                setState { it.copy(selectedNavItem = BottomNavItem.REMINDERS) }
                navigator.navigateTo(Route.Reminder)
            }
            BottomNavItem.AI_CHAT -> {
                setState { it.copy(selectedNavItem = BottomNavItem.AI_CHAT) }
                navigator.navigateTo(Route.AiChat)
            }
        }
    }
}

