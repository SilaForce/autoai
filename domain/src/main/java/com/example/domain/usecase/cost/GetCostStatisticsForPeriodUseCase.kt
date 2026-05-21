package com.example.domain.usecase.cost

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.app.map
import com.example.domain.model.cost.CostStatistics
import com.example.domain.repository.ICostRepository
import kotlinx.coroutines.CoroutineDispatcher

data class GetCostStatisticsForPeriodParams(
    val vehicleId: String,
    val sinceMillis: Long? = null,
    val untilMillis: Long? = null,
)

class GetCostStatisticsForPeriodUseCase(
    private val repository: ICostRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<GetCostStatisticsForPeriodParams, CostStatistics>(dispatcher) {

    override suspend fun execute(params: GetCostStatisticsForPeriodParams): AppResult<CostStatistics> {
        if (params.vehicleId.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
        return repository.getCosts(vehicleId = params.vehicleId.trim()).map { costs ->
            val filtered = costs.filter { cost ->
                (params.sinceMillis == null || cost.dateMillis >= params.sinceMillis) &&
                    (params.untilMillis == null || cost.dateMillis <= params.untilMillis)
            }
            val grouped = filtered.groupBy { it.category }
            CostStatistics(
                totalAmount = filtered.sumOf { it.amount },
                amountByCategory = grouped.mapValues { (_, entries) -> entries.sumOf { it.amount } },
                countByCategory = grouped.mapValues { (_, entries) -> entries.size },
            )
        }
    }
}
