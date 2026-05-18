package com.example.domain.usecase.vehicle

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.repository.IVehicleMakesRepository
import kotlinx.coroutines.CoroutineDispatcher

class GetModelsForMakeUseCase (
    private val repository: IVehicleMakesRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<String, List<String>>(dispatcher) {

    override suspend fun execute(params: String): AppResult<List<String>> {
        if (params.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
       return repository.getModelsForMake(params)
    }
}