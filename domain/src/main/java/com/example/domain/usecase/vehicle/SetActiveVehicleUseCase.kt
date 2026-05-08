package com.example.domain.usecase.vehicle

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.repository.IVehicleRepository
import com.example.domain.util.ValidationUtil
import kotlinx.coroutines.CoroutineDispatcher

data class SetActiveVehicleParams(
    val userId: String,
    val vehicleId: String,
)

class SetActiveVehicleUseCase(
    private val repository: IVehicleRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<SetActiveVehicleParams, Unit>(dispatcher) {

    override suspend fun execute(params: SetActiveVehicleParams): AppResult<Unit> {
        if (!ValidationUtil.isValidVehicleText(params.userId) ||
            !ValidationUtil.isValidVehicleText(params.vehicleId)
        ) {
            return AppResult.Failure(DataError.Local.ValidationError)
        }

        return repository.setActiveVehicle(
            userId = params.userId.trim(),
            vehicleId = params.vehicleId.trim(),
        )
    }
}
