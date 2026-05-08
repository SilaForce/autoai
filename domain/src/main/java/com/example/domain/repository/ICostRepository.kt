package com.example.domain.repository

import com.example.domain.model.app.AppResult
import com.example.domain.model.cost.Cost

interface ICostRepository {
    suspend fun addCost(cost: Cost): AppResult<Cost>
    suspend fun getCosts(vehicleId: String): AppResult<List<Cost>>
}

