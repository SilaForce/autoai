package com.example.domain.usecase.cost

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.cost.Cost
import com.example.domain.model.cost.CostCategory
import com.example.domain.datasource.CostDataSource
import kotlinx.coroutines.CoroutineDispatcher

data class UpdateCostParams(
    val costId: String,
    val userId: String,
    val vehicleId: String,
    val category: CostCategory,
    val amount: Double,
    val location: String?,
    val description: String?,
    val dateMillis: Long,
)

class UpdateCostUseCase(
    private val repository: CostDataSource,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<UpdateCostParams, Cost>(dispatcher) {

    override suspend fun execute(params: UpdateCostParams): AppResult<Cost> {
        if (params.costId.isBlank() || params.userId.isBlank() || params.vehicleId.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
        if (params.amount <= 0.0) {
            return AppResult.Failure(DataError.Local.Validation.InvalidAmount)
        }
        if (params.dateMillis <= 0L) {
            return AppResult.Failure(DataError.Local.Validation.InvalidDate)
        }

        return repository.updateCost(
            Cost(
                id = params.costId.trim(),
                userId = params.userId.trim(),
                vehicleId = params.vehicleId.trim(),
                category = params.category,
                amount = params.amount,
                location = params.location?.trim()?.takeIf { it.isNotEmpty() },
                description = params.description?.trim()?.takeIf { it.isNotEmpty() },
                dateMillis = params.dateMillis,
            )
        )
    }
}
