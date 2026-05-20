package com.example.autoai.presentation.features.costs

import com.example.autoai.presentation.components.BottomNavItem
import com.example.domain.model.cost.CostCategory

sealed interface CostsEvent {
    data class OnTabSelected(val tab: CostsTab) : CostsEvent
    data object OnAddCostClicked : CostsEvent
    data object OnAddSheetDismissed : CostsEvent
    data class OnCategorySelected(val category: CostCategory) : CostsEvent
    data class OnAmountChanged(val value: String) : CostsEvent
    data class OnLocationChanged(val value: String) : CostsEvent
    data class OnDescriptionChanged(val value: String) : CostsEvent
    data object OnSaveCostClicked : CostsEvent
    data class OnNavItemSelected(val item: BottomNavItem) : CostsEvent

    data class OnCostLongPressed(val costId: String) : CostsEvent
    data object OnDismissCostMenu : CostsEvent
    data class OnEditCostClicked(val costId: String) : CostsEvent
    data class OnDeleteCostClicked(val costId: String) : CostsEvent
    data object OnConfirmDeleteCost : CostsEvent
    data object OnDismissDeleteDialog : CostsEvent
}

