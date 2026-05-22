package com.example.autoai.presentation.features.costs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.autoai.R
import com.example.autoai.base.BaseViewModel
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.util.UiText
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.model.cost.Cost
import com.example.domain.model.cost.CostCategory
import com.example.domain.usecase.cost.AddCostParams
import com.example.domain.usecase.cost.AddCostUseCase
import com.example.domain.usecase.cost.DeleteCostUseCase
import com.example.domain.usecase.cost.GetCostsHistoryParams
import com.example.domain.usecase.cost.GetCostsHistoryUseCase
import com.example.domain.usecase.cost.GetCostStatisticsForPeriodParams
import com.example.domain.usecase.cost.GetCostStatisticsForPeriodUseCase
import com.example.domain.usecase.cost.UpdateCostParams
import com.example.domain.usecase.cost.UpdateCostUseCase
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.GetVehiclesParams
import com.example.domain.usecase.vehicle.GetVehiclesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Locale

class CostsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val getCostsHistoryUseCase: GetCostsHistoryUseCase,
    private val getCostStatisticsForPeriodUseCase: GetCostStatisticsForPeriodUseCase,
    private val addCostUseCase: AddCostUseCase,
    private val updateCostUseCase: UpdateCostUseCase,
    private val deleteCostUseCase: DeleteCostUseCase,
    private val navigator: IAppNavigator,
) : BaseViewModel<CostsState, CostsEvent, CostsSideEffect>(createInitialState(savedStateHandle)) {

    private var currentUserId: String? = null
    private var activeVehicleId: String? = null
    private var domainCosts: List<Cost> = emptyList()
    private var statsReloadJob: Job? = null
    private var statsLoadedForPeriod: StatsPeriod? = null

    override fun onEvent(event: CostsEvent) {
        when (event) {
            CostsEvent.OnScreenResumed -> loadData()

            is CostsEvent.OnTabSelected -> {
                setState { it.copy(selectedTab = event.tab) }
                val period = state.value.selectedPeriod
                if (event.tab == CostsTab.STATISTICS &&
                    activeVehicleId != null &&
                    !state.value.isLoading &&
                    statsLoadedForPeriod != period
                ) {
                    launchStatsReload(period)
                }
            }

            is CostsEvent.OnPeriodSelected -> {
                setState { it.copy(selectedPeriod = event.period) }
                launchStatsReload(event.period)
            }

            CostsEvent.OnAddCostClicked -> setState { it.copy(isSheetOpen = true) }

            CostsEvent.OnAddSheetDismissed -> resetSheet()

            is CostsEvent.OnCategorySelected -> setState { it.copy(selectedCategory = event.category) }

            is CostsEvent.OnAmountChanged -> setState { it.copy(amountInput = event.value) }

            is CostsEvent.OnLocationChanged -> setState { it.copy(locationInput = event.value) }

            is CostsEvent.OnDescriptionChanged -> setState { it.copy(descriptionInput = event.value) }

            CostsEvent.OnSaveCostClicked -> saveCost()

            is CostsEvent.OnNavItemSelected -> handleBottomNavigation(event.item)

            is CostsEvent.OnCostLongPressed -> setState { it.copy(costMenuId = event.costId) }

            CostsEvent.OnDismissCostMenu -> setState { it.copy(costMenuId = null) }

            is CostsEvent.OnEditCostClicked -> startEditingCost(event.costId)

            is CostsEvent.OnDeleteCostClicked -> setState {
                it.copy(costMenuId = null, pendingDeleteCostId = event.costId)
            }

            CostsEvent.OnDismissDeleteDialog -> setState { it.copy(pendingDeleteCostId = null) }

            CostsEvent.OnConfirmDeleteCost -> deletePendingCost()
        }
    }

    // ─── Edit / Delete ───────────────────────────────────────────────────────

    private fun startEditingCost(costId: String) {
        val cost = domainCosts.firstOrNull { it.id == costId } ?: return
        savedStateHandle[KEY_EDITING_COST_ID] = cost.id
        savedStateHandle[KEY_EDITING_ORIGINAL_DATE_MILLIS] = cost.dateMillis
        setState {
            it.copy(
                costMenuId = null,
                isSheetOpen = true,
                selectedCategory = cost.category,
                amountInput = formatAmountForInput(cost.amount),
                locationInput = cost.location.orEmpty(),
                descriptionInput = cost.description.orEmpty(),
                editingCostId = cost.id,
                editingCostOriginalDateMillis = cost.dateMillis,
            )
        }
    }

    private fun deletePendingCost() {
        val costId = state.value.pendingDeleteCostId ?: return
        val vehicleId = activeVehicleId ?: return
        setState { it.copy(pendingDeleteCostId = null) }

        viewModelScope.launch {
            deleteCostUseCase(costId)
                .onSuccess {
                    emitSideEffect(CostsSideEffect.ShowSuccess(UiText.StringResource(R.string.costs_delete_success)))
                    refreshData(vehicleId)
                }
                .onFailure { error ->
                    emitSideEffect(CostsSideEffect.ShowError(error.asUiText()))
                }
        }
    }

    // ─── Loading ─────────────────────────────────────────────────────────────

    private fun loadData() {
        setState { it.copy(isLoading = true) }

        viewModelScope.launch {
            val user = when (val result = getCurrentUserUseCase(Unit)) {
                is AppResult.Success -> result.data
                is AppResult.Failure -> {
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(CostsSideEffect.ShowError(result.error.asUiText()))
                    return@launch
                }
            }
            currentUserId = user.id

            val vehicles = when (val result = getVehiclesUseCase(GetVehiclesParams(user.id))) {
                is AppResult.Success -> result.data
                is AppResult.Failure -> {
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(CostsSideEffect.ShowError(result.error.asUiText()))
                    return@launch
                }
            }

            val activeVehicle = vehicles.firstOrNull { it.isActive }
            if (activeVehicle == null) {
                setState { it.copy(isLoading = false, hasNoActiveVehicle = true) }
                return@launch
            }

            activeVehicleId = activeVehicle.id
            setState { it.copy(hasNoActiveVehicle = false) }
            refreshData(activeVehicle.id)
            setState { it.copy(isLoading = false) }
        }
    }

    private suspend fun refreshData(vehicleId: String) {
        setState { it.copy(isRefreshing = true) }

        val period = state.value.selectedPeriod
        var historyUi: List<CostItemUi>? = null
        var statsUi: CostStatsUi? = null

        getCostsHistoryUseCase(GetCostsHistoryParams(vehicleId))
            .onSuccess { costs ->
                domainCosts = costs
                historyUi = costs.map { it.toCostItemUi() }
                getCostStatisticsForPeriodUseCase(
                    GetCostStatisticsForPeriodParams(
                        costs = costs,
                        sinceMillis = period.sinceMillis(),
                        untilMillis = null,
                    )
                ).onSuccess { stats ->
                    statsUi = stats.toCostStatsUi()
                    statsLoadedForPeriod = period
                }
            }
            .onFailure { error -> emitSideEffect(CostsSideEffect.ShowError(error.asUiText())) }

        setState { current ->
            current.copy(
                isRefreshing = false,
                history = historyUi ?: current.history,
                stats = statsUi ?: current.stats,
            )
        }
    }

    private fun launchStatsReload(period: StatsPeriod) {
        statsReloadJob?.cancel()
        statsReloadJob = viewModelScope.launch { reloadStats(period) }
    }

    private suspend fun reloadStats(period: StatsPeriod) {
        getCostStatisticsForPeriodUseCase(
            GetCostStatisticsForPeriodParams(
                costs = domainCosts,
                sinceMillis = period.sinceMillis(),
                untilMillis = null,
            )
        )
            .onSuccess { stats ->
                setState { it.copy(stats = stats.toCostStatsUi()) }
                statsLoadedForPeriod = period
            }
            .onFailure { error -> emitSideEffect(CostsSideEffect.ShowError(error.asUiText())) }
    }

    // ─── Saving ──────────────────────────────────────────────────────────────

    private fun saveCost() {
        val userId = currentUserId ?: run {
            emitSideEffect(CostsSideEffect.ShowError(UiText.StringResource(R.string.error_unknown)))
            return
        }

        val vehicleId = activeVehicleId ?: run {
            emitSideEffect(CostsSideEffect.ShowError(UiText.StringResource(R.string.error_unknown)))
            return
        }

        val amount = parseAmount(state.value.amountInput)
        if (amount == null || amount <= 0.0) {
            emitSideEffect(CostsSideEffect.ShowError(UiText.StringResource(R.string.costs_error_invalid_amount)))
            return
        }

        setState { it.copy(isSaving = true) }

        val editingId = state.value.editingCostId
        val editingDate = state.value.editingCostOriginalDateMillis

        viewModelScope.launch {
            if (editingId != null && editingDate != null) {
                updateCostUseCase(
                    UpdateCostParams(
                        costId = editingId,
                        userId = userId,
                        vehicleId = vehicleId,
                        category = state.value.selectedCategory,
                        amount = amount,
                        location = state.value.locationInput.takeIf { it.isNotBlank() },
                        description = state.value.descriptionInput.takeIf { it.isNotBlank() },
                        dateMillis = editingDate,
                    )
                ).onSuccess {
                    resetSheet(isSaving = false)
                    emitSideEffect(CostsSideEffect.ShowSuccess(UiText.StringResource(R.string.costs_update_success)))
                    refreshData(vehicleId)
                }.onFailure { error ->
                    setState { it.copy(isSaving = false) }
                    emitSideEffect(CostsSideEffect.ShowError(error.asUiText()))
                }
            } else {
                addCostUseCase(
                    AddCostParams(
                        userId = userId,
                        vehicleId = vehicleId,
                        category = state.value.selectedCategory,
                        amount = amount,
                        location = state.value.locationInput.takeIf { it.isNotBlank() },
                        description = state.value.descriptionInput.takeIf { it.isNotBlank() },
                        dateMillis = System.currentTimeMillis(),
                    )
                ).onSuccess {
                    resetSheet(isSaving = false)
                    emitSideEffect(CostsSideEffect.ShowSuccess(UiText.StringResource(R.string.costs_success)))
                    refreshData(vehicleId)
                }.onFailure { error ->
                    setState { it.copy(isSaving = false) }
                    emitSideEffect(CostsSideEffect.ShowError(error.asUiText()))
                }
            }
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun parseAmount(input: String): Double? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        val format = NumberFormat.getInstance(Locale.getDefault())
        val position = ParsePosition(0)
        val parsed = format.parse(trimmed, position) ?: return null
        return if (position.index == trimmed.length) parsed.toDouble() else null
    }

    private fun formatAmountForInput(amount: Double): String {
        val format = NumberFormat.getInstance(Locale.getDefault()).apply {
            isGroupingUsed = false
            maximumFractionDigits = 2
        }
        return format.format(amount)
    }

    private fun resetSheet(isSaving: Boolean = false) {
        savedStateHandle.remove<String>(KEY_EDITING_COST_ID)
        savedStateHandle.remove<Long>(KEY_EDITING_ORIGINAL_DATE_MILLIS)
        setState {
            it.copy(
                isSaving = isSaving,
                isSheetOpen = false,
                amountInput = "",
                locationInput = "",
                descriptionInput = "",
                selectedCategory = CostCategory.FUEL,
                editingCostId = null,
                editingCostOriginalDateMillis = null,
            )
        }
    }

    private fun handleBottomNavigation(item: BottomNavItem) {
        when (item) {
            BottomNavItem.HOME -> navigator.navigateTo(Route.Home)
            BottomNavItem.GARAGE -> navigator.navigateTo(Route.Garage)
            BottomNavItem.COSTS -> Unit
            BottomNavItem.REMINDERS -> navigator.navigateTo(Route.Reminder)
            BottomNavItem.AI_CHAT -> navigator.navigateTo(Route.AiChat)
        }
    }

    private companion object {
        const val KEY_EDITING_COST_ID = "costs_editing_cost_id"
        const val KEY_EDITING_ORIGINAL_DATE_MILLIS = "costs_editing_original_date_millis"

        fun createInitialState(savedStateHandle: SavedStateHandle): CostsState {
            val editingId = savedStateHandle.get<String>(KEY_EDITING_COST_ID)
            val editingDate = savedStateHandle.get<Long>(KEY_EDITING_ORIGINAL_DATE_MILLIS)
            return CostsState(
                editingCostId = editingId,
                editingCostOriginalDateMillis = editingDate,
            )
        }
    }
}
