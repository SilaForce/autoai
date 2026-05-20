package com.example.domain.repository

import com.example.domain.model.app.AppResult
import com.example.domain.model.vehicle.Vehicle

interface IVehicleRepository {
    suspend fun addVehicle(vehicle: Vehicle): AppResult<Vehicle>
    suspend fun getVehicles(userId: String): AppResult<List<Vehicle>>
    suspend fun getVehicleById(vehicleId: String): AppResult<Vehicle>
    suspend fun updateVehicle(vehicle: Vehicle): AppResult<Vehicle>
    suspend fun deleteVehicle(vehicleId: String): AppResult<Unit>
    suspend fun setActiveVehicle(userId: String, vehicleId: String): AppResult<Unit>
}
