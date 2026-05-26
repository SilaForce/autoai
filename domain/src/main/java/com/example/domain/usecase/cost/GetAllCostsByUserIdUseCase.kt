package com.example.domain.usecase.cost

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.cost.Cost
import com.example.domain.datasource.CostDataSource
import kotlinx.coroutines.CoroutineDispatcher

data class GetAllCostsByUserIdParams(
    val userId: String,
)

class GetAllCostsByUserIdUseCase(
    private val repository: CostDataSource,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<GetAllCostsByUserIdParams, List<Cost>>(dispatcher) {

    override suspend fun execute(params: GetAllCostsByUserIdParams): AppResult<List<Cost>> {
        if (params.userId.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
        return repository.getCostsByUserId(userId = params.userId.trim())
    }
}
