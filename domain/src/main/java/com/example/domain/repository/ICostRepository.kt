package com.example.domain.repository

import com.example.domain.model.app.AppResult
import com.example.domain.model.cost.Cost

interface ICostRepository {
    suspend fun addCost(cost: Cost): AppResult<Cost>
    suspend fun getCosts(vehicleId: String): AppResult<List<Cost>>
    suspend fun getCostsByUserId(userId: String): AppResult<List<Cost>>
    suspend fun updateCost(cost: Cost): AppResult<Cost>
    suspend fun deleteCost(costId: String): AppResult<Unit>
}

