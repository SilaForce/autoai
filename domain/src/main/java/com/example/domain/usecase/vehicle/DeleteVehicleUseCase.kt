package com.example.domain.usecase.vehicle

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.repository.IVehicleRepository
import com.example.domain.util.ValidationUtil
import kotlinx.coroutines.CoroutineDispatcher

class DeleteVehicleUseCase(
    private val repository: IVehicleRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<String, Unit>(dispatcher) {

    override suspend fun execute(params: String): AppResult<Unit> {
        if (!ValidationUtil.isValidVehicleText(params)) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
        return repository.deleteVehicle(params.trim())
    }
}
