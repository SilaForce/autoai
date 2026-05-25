package com.example.domain.usecase.vehicle

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.app.andThen
import com.example.domain.repository.ICostRepository
import com.example.domain.repository.IRemindersRepository
import com.example.domain.repository.IVehicleRepository
import com.example.domain.util.ValidationUtil
import kotlinx.coroutines.CoroutineDispatcher

data class DeleteVehicleParams(
    val userId: String,
    val vehicleId: String,
)

class DeleteVehicleUseCase(
    private val vehicleRepository: IVehicleRepository,
    private val costRepository: ICostRepository,
    private val reminderRepository: IRemindersRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<DeleteVehicleParams, Unit>(dispatcher) {

    override suspend fun execute(params: DeleteVehicleParams): AppResult<Unit> {
        if (!ValidationUtil.isValidVehicleText(params.userId) ||
            !ValidationUtil.isValidVehicleText(params.vehicleId)
        ) {
            return AppResult.Failure(DataError.Local.NotFound)
        }
        val vehicleId = params.vehicleId.trim()
        val userId = params.userId.trim()

        // Cascade order: children first, parent last. If parent delete fails after
        // children are gone, the user can retry; if children fail first, the
        // vehicle is still intact and the operation is recoverable.
        return costRepository.deleteCostsForVehicle(vehicleId)
            .andThen { reminderRepository.deleteRemindersForVehicle(vehicleId) }
            .andThen { vehicleRepository.deleteVehicle(userId, vehicleId) }
    }
}
