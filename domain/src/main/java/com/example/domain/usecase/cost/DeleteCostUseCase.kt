package com.example.domain.usecase.cost

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.datasource.CostDataSource
import kotlinx.coroutines.CoroutineDispatcher

class DeleteCostUseCase(
    private val repository: CostDataSource,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<String, Unit>(dispatcher) {

    override suspend fun execute(params: String): AppResult<Unit> {
        if (params.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
        return repository.deleteCost(params.trim())
    }
}
