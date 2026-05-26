package com.example.domain.usecase.vehicle

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.app.andThen
import com.example.domain.datasource.CostDataSource
import com.example.domain.datasource.RemindersDataSource
import com.example.domain.datasource.VehicleDataSource
import com.example.domain.util.ValidationUtil
import kotlinx.coroutines.CoroutineDispatcher

data class DeleteVehicleParams(
    val userId: String,
    val vehicleId: String,
)

class DeleteVehicleUseCase(
    private val vehicleDataSource: VehicleDataSource,
    private val costDataSource: CostDataSource,
    private val reminderDataSource: RemindersDataSource,
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
        return costDataSource.deleteCostsForVehicle(vehicleId)
            .andThen { reminderDataSource.deleteRemindersForVehicle(vehicleId) }
            .andThen { vehicleDataSource.deleteVehicle(userId, vehicleId) }
    }
}
