package com.example.data.model.cost

data class CostDto(
    val id: String = "",
    val userId: String = "",
    val vehicleId: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val location: String? = null,
    val description: String? = null,
    val dateMillis: Long = 0L,
)

