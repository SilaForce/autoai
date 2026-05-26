package com.example.domain.usecase.cost

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.cost.Cost
import com.example.domain.model.cost.CostCategory
import com.example.domain.datasource.CostDataSource
import kotlinx.coroutines.CoroutineDispatcher

data class AddCostParams(
    val userId: String,
    val vehicleId: String,
    val category: CostCategory,
    val amount: Double,
    val location: String?,
    val description: String?,
    val dateMillis: Long,
)

class AddCostUseCase(
    private val repository: CostDataSource,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<AddCostParams, Cost>(dispatcher) {

    override suspend fun execute(params: AddCostParams): AppResult<Cost> {
        if (params.userId.isBlank() || params.vehicleId.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
        if (params.amount <= 0.0) {
            return AppResult.Failure(DataError.Local.Validation.InvalidAmount)
        }
        if (params.dateMillis <= 0L) {
            return AppResult.Failure(DataError.Local.Validation.InvalidDate)
        }

        return repository.addCost(
            Cost(
                id = "",
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
