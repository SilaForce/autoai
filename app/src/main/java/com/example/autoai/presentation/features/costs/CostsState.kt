package com.example.autoai.presentation.features.costs

import androidx.compose.runtime.Stable
import com.example.autoai.presentation.components.BottomNavItem
import com.example.domain.model.cost.CostCategory

@Stable
data class CostsState(
    val isLoading: Boolean = false,
    val hasNoActiveVehicle: Boolean = false,
    val selectedTab: CostsTab = CostsTab.HISTORY,
    val history: List<CostItemUi> = emptyList(),
    val stats: CostStatsUi? = null,
    val selectedNavItem: BottomNavItem = BottomNavItem.COSTS,

    // Add-cost bottom sheet
    val isSheetOpen: Boolean = false,
    val selectedCategory: CostCategory = CostCategory.FUEL,
    val amountInput: String = "",
    val locationInput: String = "",
    val descriptionInput: String = "",
    val isSaving: Boolean = false,

    // Edit/Delete
    val costMenuId: String? = null,
    val pendingDeleteCostId: String? = null,
    val editingCostId: String? = null,
    val editingCostOriginalDateMillis: Long? = null,
)

