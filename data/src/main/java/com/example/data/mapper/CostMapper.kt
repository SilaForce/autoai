package com.example.data.mapper

import com.example.data.model.cost.CostDto
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.cost.Cost
import com.example.domain.model.cost.CostCategory

fun CostDto.toCost(): AppResult<Cost> {
    val parsedCategory = runCatching {
        CostCategory.valueOf(category)
    }.getOrElse {
        return AppResult.Failure(DataError.Network.Serialization)
    }

    if (vehicleId.isBlank()) {
        return AppResult.Failure(DataError.Network.Serialization)
    }

    return AppResult.Success(
        Cost(
            id = id,
            userId = userId,
            vehicleId = vehicleId,
            category = parsedCategory,
            amount = amount,
            location = location,
            description = description,
            dateMillis = dateMillis,
        )
    )
}

fun Cost.toCostDto(): CostDto {
    return CostDto(
        id = id,
        userId = userId,
        vehicleId = vehicleId,
        category = category.name,
        amount = amount,
        location = location,
        description = description,
        dateMillis = dateMillis,
    )
}
