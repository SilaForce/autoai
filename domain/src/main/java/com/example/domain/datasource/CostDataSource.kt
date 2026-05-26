package com.example.domain.datasource

import com.example.domain.model.app.AppResult
import com.example.domain.model.cost.Cost

interface CostDataSource {
    suspend fun addCost(cost: Cost): AppResult<Cost>
    suspend fun getCosts(vehicleId: String): AppResult<List<Cost>>
    suspend fun getCostsByUserId(userId: String): AppResult<List<Cost>>
    suspend fun updateCost(cost: Cost): AppResult<Cost>
    suspend fun deleteCost(costId: String): AppResult<Unit>
    suspend fun deleteCostsForVehicle(vehicleId: String): AppResult<Unit>
    suspend fun deleteAllForUser(userId: String): AppResult<Unit>
}

