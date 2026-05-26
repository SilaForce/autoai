package com.example.domain.usecase.vehicle

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.vehicle.Vehicle
import com.example.domain.datasource.VehicleDataSource
import com.example.domain.util.ValidationUtil
import kotlinx.coroutines.CoroutineDispatcher

data class GetVehiclesParams(
    val userId: String,
)

class GetVehiclesUseCase(
    private val repository: VehicleDataSource,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<GetVehiclesParams, List<Vehicle>>(dispatcher) {

    override suspend fun execute(params: GetVehiclesParams): AppResult<List<Vehicle>> {
        // Defense-in-depth: a blank userId means the auth context wasn't resolved.
        // Don't hit Firestore — return NotFound so the UI can recover gracefully.
        if (!ValidationUtil.isValidVehicleText(params.userId)) {
            return AppResult.Failure(DataError.Local.NotFound)
        }

        return repository.getVehicles(userId = params.userId.trim())
    }
}
