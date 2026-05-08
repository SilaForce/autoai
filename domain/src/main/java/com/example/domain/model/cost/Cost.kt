package com.example.domain.model.cost

data class Cost(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val category: CostCategory,
    val amount: Double,
    val location: String?,
    val description: String?,
    val dateMillis: Long,
)

