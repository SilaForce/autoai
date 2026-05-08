package com.example.domain.model.vehicle

data class Vehicle(
    val id: String,
    val userId: String,
    val make: String,
    val model: String,
    val year: Int,
    val fuelType: FuelType,
    val mileage: Int? = null,
    val licensePlate: String? = null,
    val isActive: Boolean = false,
)
