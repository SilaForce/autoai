package com.example.domain.datasource

import com.example.domain.model.app.AppResult

interface VehicleMakesDataSource {
        suspend fun getCarMakes(): AppResult<List<String>>
        suspend fun getModelsForMake(make: String): AppResult<List<String>>
}