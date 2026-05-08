package com.example.autoai.presentation.features.costs

data class CostStatsByCategoryUi(
    val categoryName: String,
    val amount: String,
    val progress: Float, // 0.0 – 1.0 relative to the highest-value category
)

data class CostStatsUi(
    val totalAmount: String,
    val categoryBreakdowns: List<CostStatsByCategoryUi>,
)

