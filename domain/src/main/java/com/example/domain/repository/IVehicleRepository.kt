package com.example.domain.repository

import com.example.domain.model.app.AppResult
import com.example.domain.model.vehicle.Vehicle
import kotlinx.coroutines.flow.Flow

interface IVehicleRepository {
    suspend fun addVehicle(vehicle: Vehicle): AppResult<Vehicle>
    suspend fun getVehicles(userId: String): AppResult<List<Vehicle>>
    suspend fun getVehicleById(vehicleId: String): AppResult<Vehicle>
    suspend fun updateVehicle(vehicle: Vehicle): AppResult<Vehicle>
    suspend fun deleteVehicle(userId: String, vehicleId: String): AppResult<Unit>
    suspend fun setActiveVehicle(userId: String, vehicleId: String): AppResult<Unit>
    suspend fun deleteAllForUser(userId: String): AppResult<Unit>
    fun observeActiveVehicle(userId: String): Flow<AppResult<Vehicle?>>
}
