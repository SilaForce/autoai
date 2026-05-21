package com.example.domain.model.cost

data class CostStatistics(
    val totalAmount: Double,
    val amountByCategory: Map<CostCategory, Double>,
    val countByCategory: Map<CostCategory, Int> = emptyMap(),
)

