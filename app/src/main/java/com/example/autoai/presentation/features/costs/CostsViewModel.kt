package com.example.autoai.presentation.features.costs

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
import com.example.domain.model.cost.Cost
import com.example.domain.model.cost.CostCategory
import com.example.domain.usecase.cost.AddCostParams
import com.example.domain.usecase.cost.AddCostUseCase
import com.example.domain.usecase.cost.DeleteCostUseCase
import com.example.domain.usecase.cost.GetCostsHistoryParams
import com.example.domain.usecase.cost.GetCostsHistoryUseCase
import com.example.domain.usecase.cost.GetCostStatisticsParams
import com.example.domain.usecase.cost.GetCostStatisticsUseCase
import com.example.domain.usecase.cost.UpdateCostParams
import com.example.domain.usecase.cost.UpdateCostUseCase
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.GetVehiclesParams
import com.example.domain.usecase.vehicle.GetVehiclesUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CostsViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val getCostsHistoryUseCase: GetCostsHistoryUseCase,
    private val getCostStatisticsUseCase: GetCostStatisticsUseCase,
    private val addCostUseCase: AddCostUseCase,
    private val updateCostUseCase: UpdateCostUseCase,
    private val deleteCostUseCase: DeleteCostUseCase,
    private val navigator: IAppNavigator,
) : BaseViewModel<CostsState, CostsEvent, CostsSideEffect>(CostsState()) {

    private var currentUserId: String? = null
    private var activeVehicleId: String? = null
    private var domainCosts: List<Cost> = emptyList()

    init {
        loadData()
    }

    override fun onEvent(event: CostsEvent) {
        when (event) {
            is CostsEvent.OnTabSelected -> setState { it.copy(selectedTab = event.tab) }

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
        setState {
            it.copy(
                costMenuId = null,
                isSheetOpen = true,
                selectedCategory = cost.category,
                amountInput = cost.amount.toString(),
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
            // Step 1: resolve the current user
            getCurrentUserUseCase(Unit)
                .onFailure { error ->
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(CostsSideEffect.ShowError(error.asUiText()))
                    return@launch
                }
                .onSuccess { user ->
                    currentUserId = user.id

                    // Step 2: find the active vehicle for this user
                    getVehiclesUseCase(GetVehiclesParams(user.id))
                        .onFailure { error ->
                            setState { it.copy(isLoading = false) }
                            emitSideEffect(CostsSideEffect.ShowError(error.asUiText()))
                            return@launch
                        }
                        .onSuccess { vehicles ->
                            val activeVehicle = vehicles.firstOrNull { it.isActive }

                            if (activeVehicle == null) {
                                // No active vehicle — show the empty/no-vehicle state
                                setState { it.copy(isLoading = false, hasNoActiveVehicle = true) }
                                return@launch
                            }

                            activeVehicleId = activeVehicle.id
                            setState { it.copy(hasNoActiveVehicle = false) }
                            refreshData(activeVehicle.id)
                        }
                }
        }
    }

    private fun refreshData(vehicleId: String) {
        viewModelScope.launch {
            setState { it.copy(isLoading = true) }

            val historyDeferred = async { getCostsHistoryUseCase(GetCostsHistoryParams(vehicleId)) }
            val statsDeferred = async { getCostStatisticsUseCase(GetCostStatisticsParams(vehicleId)) }

            val historyResult = historyDeferred.await()
            val statsResult = statsDeferred.await()

            var historyUi = state.value.history
            var statsUi = state.value.stats

            historyResult
                .onSuccess { costs ->
                    domainCosts = costs
                    historyUi = costs.map { it.toCostItemUi() }
                }
                .onFailure { error -> emitSideEffect(CostsSideEffect.ShowError(error.asUiText())) }

            statsResult
                .onSuccess { stats -> statsUi = stats.toCostStatsUi() }
                .onFailure { error -> emitSideEffect(CostsSideEffect.ShowError(error.asUiText())) }

            setState { it.copy(isLoading = false, history = historyUi, stats = statsUi) }
        }
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

        val amount = state.value.amountInput.toDoubleOrNull()
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

    private fun resetSheet(isSaving: Boolean = false) {
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
            BottomNavItem.HOME -> {
                setState { it.copy(selectedNavItem = BottomNavItem.HOME) }
                navigator.navigateTo(Route.Home)
            }
            BottomNavItem.GARAGE -> {
                setState { it.copy(selectedNavItem = BottomNavItem.GARAGE) }
                navigator.navigateTo(Route.Garage)
            }
            BottomNavItem.COSTS -> setState { it.copy(selectedNavItem = BottomNavItem.COSTS) }
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
