package com.example.domain.repository

import com.example.domain.model.app.AppResult

interface IVehicleMakesRepository {
        suspend fun getCarMakes(): AppResult<List<String>>
        suspend fun getModelsForMake(make: String): AppResult<List<String>>
}