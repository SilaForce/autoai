package com.example.domain.usecase.cost

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.cost.Cost
import com.example.domain.model.cost.CostStatistics
import kotlinx.coroutines.CoroutineDispatcher

data class GetCostStatisticsForPeriodParams(
    val costs: List<Cost>,
    val sinceMillis: Long? = null,
    val untilMillis: Long? = null,
)

class GetCostStatisticsForPeriodUseCase(
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<GetCostStatisticsForPeriodParams, CostStatistics>(dispatcher) {

    override suspend fun execute(params: GetCostStatisticsForPeriodParams): AppResult<CostStatistics> {
        val filtered = params.costs.filter { cost ->
            (params.sinceMillis == null || cost.dateMillis >= params.sinceMillis) &&
                (params.untilMillis == null || cost.dateMillis <= params.untilMillis)
        }
        val grouped = filtered.groupBy { it.category }
        return AppResult.Success(
            CostStatistics(
                totalAmount = filtered.sumOf { it.amount },
                amountByCategory = grouped.mapValues { (_, entries) -> entries.sumOf { it.amount } },
                countByCategory = grouped.mapValues { (_, entries) -> entries.size },
            )
        )
    }
}
