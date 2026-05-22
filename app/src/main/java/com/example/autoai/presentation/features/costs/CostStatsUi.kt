package com.example.autoai.presentation.features.costs

import androidx.compose.runtime.Immutable
import com.example.autoai.presentation.util.UiText
import com.example.domain.model.cost.CostCategory

@Immutable
data class CostStatsByCategoryUi(
    val category: CostCategory,
    val categoryName: UiText,
    val amount: String,
    val percentage: Int,
    val count: Int,
    val averagePerEntry: String,
    val progress: Float, // amount / totalAmount, 0f..1f
)

@Immutable
data class CostStatsUi(
    val totalAmount: String,
    val categoryBreakdowns: List<CostStatsByCategoryUi>,
)
