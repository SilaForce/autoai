package com.example.autoai.presentation.features.home

import androidx.lifecycle.viewModelScope
import com.example.autoai.base.BaseViewModel
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.usecase.cost.GetCostStatisticsParams
import com.example.domain.usecase.cost.GetCostStatisticsUseCase
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.GetVehiclesParams
import com.example.domain.usecase.vehicle.GetVehiclesUseCase
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.floor

class HomeViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val getCostStatisticsUseCase: GetCostStatisticsUseCase,
    private val navigator: IAppNavigator,
) : BaseViewModel<HomeState, HomeEvent, HomeSideEffect>(HomeState()) {

    private var currentUserId: String? = null
    private var activeVehicleId: String? = null

    init {
        loadUser()
    }

    override fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.OnScreenResumed -> refreshData()

            HomeEvent.OnVehicleClicked -> navigator.navigateTo(Route.Garage)

            HomeEvent.OnFuelClicked,
            HomeEvent.OnServiceClicked -> navigator.navigateTo(Route.Costs)

            HomeEvent.OnAiClicked -> { navigator.navigateTo(Route.AiChat) }

            HomeEvent.OnProfileClicked -> navigator.navigateTo(Route.Profile)

            is HomeEvent.OnNavItemSelected -> handleBottomNavigation(event.item)
        }
    }

    // ─── Loading ─────────────────────────────────────────────────────────────

    private fun loadUser() {
        setState { it.copy(isLoading = true) }

        viewModelScope.launch {
            getCurrentUserUseCase(Unit)
                .onFailure { error ->
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(HomeSideEffect.ShowError(error.asUiText()))
                }
                .onSuccess { user ->
                    currentUserId = user.id
                    setState { it.copy(userName = user.name) }
                    loadActiveVehicleAndStats(user.id)
                }
        }
    }

    /** Silently refresh after returning from another screen (no full-screen spinner). */
    private fun refreshData() {
        val userId = currentUserId ?: run { loadUser(); return }
        viewModelScope.launch {
            loadActiveVehicleAndStats(userId)
        }
    }

    private suspend fun loadActiveVehicleAndStats(userId: String) {
        getVehiclesUseCase(GetVehiclesParams(userId))
            .onFailure { error ->
                setState { it.copy(isLoading = false) }
                emitSideEffect(HomeSideEffect.ShowError(error.asUiText()))
            }
            .onSuccess { vehicles ->
                val active = vehicles.firstOrNull { it.isActive }

                if (active == null) {
                    activeVehicleId = null
                    setState {
                        it.copy(
                            isLoading = false,
                            hasActiveVehicle = false,
                            activeVehicleName = "",
                            activeVehiclePlate = "",
                            totalExpenses = "0",
                        )
                    }
                    return@onSuccess
                }

                activeVehicleId = active.id
                setState {
                    it.copy(
                        hasActiveVehicle = true,
                        activeVehicleName = "${active.make} ${active.model}",
                        activeVehiclePlate = active.licensePlate.orEmpty(),
                    )
                }

                loadCostStats(active.id)
            }
    }

    private suspend fun loadCostStats(vehicleId: String) {
        getCostStatisticsUseCase(GetCostStatisticsParams(vehicleId))
            .onSuccess { stats ->
                setState {
                    it.copy(
                        isLoading = false,
                        totalExpenses = stats.totalAmount.formatAmount(),
                    )
                }
            }
            .onFailure {
                // Non-fatal: show 0 but don't block the screen
                setState { it.copy(isLoading = false, totalExpenses = "0") }
            }
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    private fun handleBottomNavigation(item: BottomNavItem) {
        when (item) {
            BottomNavItem.HOME -> setState { it.copy(selectedNavItem = item) }

            BottomNavItem.GARAGE -> {
                setState { it.copy(selectedNavItem = item) }
                navigator.navigateTo(Route.Garage)
            }

            BottomNavItem.COSTS -> {
                setState { it.copy(selectedNavItem = item) }
                navigator.navigateTo(Route.Costs)
            }

            BottomNavItem.REMINDERS -> {
                setState { it.copy(selectedNavItem = item) }
                navigator.navigateTo(Route.Reminder)
            }
            BottomNavItem.AI_CHAT ->{ setState { it.copy(selectedNavItem = item) }
            navigator.navigateTo(Route.AiChat)}
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun Double.formatAmount(): String =
        if (this == floor(this)) this.toInt().toString()
        else String.format(Locale.getDefault(), "%.2f", this)
}
