package com.example.autoai.presentation.features.home

import androidx.lifecycle.viewModelScope
import com.example.autoai.base.BaseViewModel
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.usecase.cost.GetCostStatisticsParams
import com.example.domain.usecase.cost.GetCostStatisticsUseCase
import com.example.domain.usecase.reminder.GetRemindersParams
import com.example.domain.usecase.reminder.GetRemindersUseCase
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.ObserveActiveVehicleUseCase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.floor

class HomeViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val observeActiveVehicleUseCase: ObserveActiveVehicleUseCase,
    private val getCostStatisticsUseCase: GetCostStatisticsUseCase,
    private val getReminderUseCase: GetRemindersUseCase,
    private val navigator: IAppNavigator,
) : BaseViewModel<HomeState, HomeEvent, HomeSideEffect>(HomeState()) {

    private var lastObservedActiveVehicleId: String? = null

    init {
        loadUser()
    }

    override fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.OnScreenResumed -> {
                // Re-read the user so a currency change made in Settings propagates
                // to the monthly-cost card. The active-vehicle Flow handles the rest.
                viewModelScope.launch {
                    getCurrentUserUseCase(Unit).onSuccess { user ->
                        setState { it.copy(userName = user.name, currency = user.currency) }
                    }
                }
            }

            HomeEvent.OnVehicleClicked -> navigator.navigateTo(Route.Garage)

            HomeEvent.OnFuelClicked,
            HomeEvent.OnServiceClicked -> navigator.navigateTo(Route.Costs)

            HomeEvent.OnAiClicked -> { navigator.navigateTo(Route.AiChat) }

            HomeEvent.OnProfileClicked -> navigator.navigateTo(Route.Profile)

            is HomeEvent.OnNavItemSelected -> handleBottomNavigation(event.item)

            HomeEvent.OnDueReminderClicked -> {
                navigator.navigateTo(Route.Reminder)
            }

            HomeEvent.OnMonthlyCostClicked -> {
                navigator.navigateTo(Route.Costs)
            }
        }
    }

    private fun loadUser() {
        setState { it.copy(isLoading = true) }

        viewModelScope.launch {
            getCurrentUserUseCase(Unit)
                .onFailure { error ->
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(HomeSideEffect.ShowError(error.asUiText()))
                }
                .onSuccess { user ->
                    setState { it.copy(userName = user.name, currency = user.currency) }
                    subscribeToActiveVehicle(user.id)
                }
        }
    }

    private fun subscribeToActiveVehicle(userId: String) {
        viewModelScope.launch {
            observeActiveVehicleUseCase(userId).collect { result ->
                when (result) {
                    is AppResult.Success -> handleActiveVehicleEmission(result.data)
                    is AppResult.Failure -> {
                        setState { it.copy(isLoading = false) }
                        emitSideEffect(HomeSideEffect.ShowError(result.error.asUiText()))
                    }
                }
            }
        }
    }

    private suspend fun handleActiveVehicleEmission(active: com.example.domain.model.vehicle.Vehicle?) {
        if (active == null) {
            lastObservedActiveVehicleId = null
            setState {
                it.copy(
                    isLoading = false,
                    hasActiveVehicle = false,
                    activeVehicleName = "",
                    activeVehiclePlate = "",
                    activeVehiclePhotoBase64 = null,
                    totalExpenses = EMPTY_EXPENSES_PLACEHOLDER,
                )
            }
            return
        }

        val previousId = lastObservedActiveVehicleId
        lastObservedActiveVehicleId = active.id

        setState {
            it.copy(
                hasActiveVehicle = true,
                activeVehicleName = "${active.make} ${active.model}",
                activeVehiclePlate = active.licensePlate.orEmpty(),
                activeVehiclePhotoBase64 = active.photoBase64,
            )
        }

        // Only refresh derived data when the active vehicle id actually changes.
        // Other field updates (e.g. mileage) won't trigger redundant cost/reminder fetches.
        if (active.id != previousId) {
            loadCostStats(active.id)
            loadReminder(active.id)
        }
    }

    private suspend fun loadReminder(vehicleId: String) {
        getReminderUseCase(GetRemindersParams(vehicleId))
            .onFailure { error ->
                setState { it.copy(isLoading = false) }
                emitSideEffect(HomeSideEffect.ShowError(error.asUiText()))
            }
            .onSuccess { reminders ->
                val dueReminders = reminders.filter { !it.isCompleted && it.dueDateMillis > System.currentTimeMillis() }
                    .minByOrNull { it.dueDateMillis }
                if (dueReminders != null) {
                    setState {
                        it.copy(
                            dueReminderTitle = dueReminders.title,
                            dueReminderDate = SimpleDateFormat(
                                "dd MMMM yyyy",
                                Locale.getDefault()
                            ).format(
                                Date(dueReminders.dueDateMillis)
                            )
                        )
                    }
                } else {
                    setState { it.copy(dueReminderTitle = "Reminder", dueReminderDate = "No upcoming reminders") }
                }
            }
    }

    private suspend fun loadCostStats(vehicleId: String) {
        getCostStatisticsUseCase(GetCostStatisticsParams(vehicleId))
            .onSuccess { stats ->
                // amountByCategory is keyed by category; an empty map means literally no
                // entries logged. Total of 0.0 alone is ambiguous (could mean "logged
                // a 0-amount cost") so the map's emptiness is the right signal.
                val hasEntries = stats.amountByCategory.isNotEmpty()
                setState {
                    it.copy(
                        isLoading = false,
                        totalExpenses = if (hasEntries) stats.totalAmount.formatAmount()
                                        else EMPTY_EXPENSES_PLACEHOLDER,
                    )
                }
            }
            .onFailure {
                setState { it.copy(isLoading = false, totalExpenses = EMPTY_EXPENSES_PLACEHOLDER) }
            }
    }

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
            BottomNavItem.AI_CHAT -> {
                setState { it.copy(selectedNavItem = item) }
                navigator.navigateTo(Route.AiChat)
            }
        }
    }

    private fun Double.formatAmount(): String =
        if (this == floor(this)) this.toInt().toString()
        else String.format(Locale.getDefault(), "%.2f", this)

    private companion object {
        // Em-dash placeholder when there's nothing to display. Distinguishes "no costs
        // logged yet" from "logged 0" — both used to render as "0".
        const val EMPTY_EXPENSES_PLACEHOLDER = "—"
    }
}
