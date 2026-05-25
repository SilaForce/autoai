package com.example.domain.usecase.vehicle

import com.example.domain.model.app.AppResult
import com.example.domain.model.vehicle.Vehicle
import com.example.domain.repository.IVehicleRepository
import kotlinx.coroutines.flow.Flow

class ObserveActiveVehicleUseCase(
    private val repository: IVehicleRepository,
) {
    operator fun invoke(userId: String): Flow<AppResult<Vehicle?>> {
        return repository.observeActiveVehicle(userId)
    }
}
