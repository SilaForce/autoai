package com.example.domain.usecase.vehicle

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.vehicle.Vehicle
import com.example.domain.repository.IVehicleRepository
import com.example.domain.util.ValidationUtil
import kotlinx.coroutines.CoroutineDispatcher

data class GetVehiclesParams(
    val userId: String,
)

class GetVehiclesUseCase(
    private val repository: IVehicleRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<GetVehiclesParams, List<Vehicle>>(dispatcher) {

    override suspend fun execute(params: GetVehiclesParams): AppResult<List<Vehicle>> {
        if (!ValidationUtil.isValidVehicleText(params.userId)) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }

        return repository.getVehicles(userId = params.userId.trim())
    }
}
