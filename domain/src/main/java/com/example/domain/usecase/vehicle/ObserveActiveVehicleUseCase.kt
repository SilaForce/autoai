package com.example.domain.usecase.vehicle

import com.example.domain.model.app.AppResult
import com.example.domain.model.vehicle.Vehicle
import com.example.domain.datasource.VehicleDataSource
import kotlinx.coroutines.flow.Flow

class ObserveActiveVehicleUseCase(
    private val repository: VehicleDataSource,
) {
    operator fun invoke(userId: String): Flow<AppResult<Vehicle?>> {
        return repository.observeActiveVehicle(userId)
    }
}
