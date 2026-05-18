package com.example.domain.usecase.cost

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.app.map
import com.example.domain.model.cost.CostStatistics
import com.example.domain.repository.ICostRepository
import kotlinx.coroutines.CoroutineDispatcher

data class GetCostStatisticsParams(
    val vehicleId: String,
)

class GetCostStatisticsUseCase(
    private val repository: ICostRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<GetCostStatisticsParams, CostStatistics>(dispatcher) {

    override suspend fun execute(params: GetCostStatisticsParams): AppResult<CostStatistics> {
        if (params.vehicleId.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }

        return repository.getCosts(vehicleId = params.vehicleId.trim()).map { costs ->
            val totalAmount = costs.sumOf { it.amount }
            val amountByCategory = costs
                .groupBy { it.category }
                .mapValues { (_, entries) -> entries.sumOf { it.amount } }

            CostStatistics(
                totalAmount = totalAmount,
                amountByCategory = amountByCategory,
            )
        }
    }
}
