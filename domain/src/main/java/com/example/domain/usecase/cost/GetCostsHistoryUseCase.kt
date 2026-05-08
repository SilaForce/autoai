package com.example.domain.usecase.cost

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.cost.Cost
import com.example.domain.repository.ICostRepository
import kotlinx.coroutines.CoroutineDispatcher

data class GetCostsHistoryParams(
    val vehicleId: String,
)

class GetCostsHistoryUseCase(
    private val repository: ICostRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<GetCostsHistoryParams, List<Cost>>(dispatcher) {

    override suspend fun execute(params: GetCostsHistoryParams): AppResult<List<Cost>> {
        if (params.vehicleId.isBlank()) {
            return AppResult.Failure(DataError.Local.ValidationError)
        }
        return repository.getCosts(vehicleId = params.vehicleId.trim())
    }
}
