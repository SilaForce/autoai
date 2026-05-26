package com.example.domain.usecase.vehicle

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.vehicle.Vehicle
import com.example.domain.datasource.VehicleDataSource
import com.example.domain.util.ValidationUtil
import kotlinx.coroutines.CoroutineDispatcher

class GetVehicleByIdUseCase(
    private val repository: VehicleDataSource,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<String, Vehicle>(dispatcher) {

    override suspend fun execute(params: String): AppResult<Vehicle> {
        if (!ValidationUtil.isValidVehicleText(params)) {
            return AppResult.Failure(DataError.Local.NotFound)
        }
        return repository.getVehicleById(params.trim())
    }
}
