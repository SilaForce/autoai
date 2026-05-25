package com.example.data.mapper

import com.example.data.model.vehicle.VehicleDto
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.vehicle.FuelType
import com.example.domain.model.vehicle.Vehicle

fun VehicleDto.toVehicle(): AppResult<Vehicle> {
    val parsedFuelType = runCatching {
        FuelType.valueOf(fuelType)
    }.getOrElse {
        return AppResult.Failure(DataError.Network.Serialization)
    }

    return AppResult.Success(
        Vehicle(
            id = id,
            userId = userId,
            make = make,
            model = model,
            year = year,
            fuelType = parsedFuelType,
            mileage = mileage,
            licensePlate = licensePlate,
            isActive = isActive,
            photoBase64 = photoBase64,
        )
    )
}

fun Vehicle.toVehicleDto(): VehicleDto {
    return VehicleDto(
        id = id,
        userId = userId,
        make = make,
        model = model,
        year = year,
        fuelType = fuelType.name,
        mileage = mileage,
        licensePlate = licensePlate,
        isActive = isActive,
        photoBase64 = photoBase64,
    )
}
