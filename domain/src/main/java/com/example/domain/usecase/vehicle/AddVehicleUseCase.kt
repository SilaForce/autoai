package com.example.domain.usecase.vehicle

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.vehicle.FuelType
import com.example.domain.model.vehicle.Vehicle
import com.example.domain.repository.IVehicleRepository
import com.example.domain.util.ValidationUtil
import kotlinx.coroutines.CoroutineDispatcher

data class AddVehicleParams(
    val userId: String,
    val make: String,
    val model: String,
    val year: Int,
    val fuelType: FuelType,
    val mileage: Int? = null,
    val licensePlate: String? = null,
    val isActive: Boolean = false,
)

class AddVehicleUseCase(
    private val repository: IVehicleRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<AddVehicleParams, Vehicle>(dispatcher) {

    override suspend fun execute(params: AddVehicleParams): AppResult<Vehicle> {
        if (!ValidationUtil.isValidVehicleText(params.userId) ||
            !ValidationUtil.isValidVehicleText(params.make) ||
            !ValidationUtil.isValidVehicleText(params.model) ||
            !ValidationUtil.isValidVehicleYear(params.year) ||
            !ValidationUtil.isValidMileage(params.mileage)
        ) {
            return AppResult.Failure(DataError.Local.ValidationError)
        }

        return repository.addVehicle(
            Vehicle(
                id = "",
                userId = params.userId.trim(),
                make = params.make.trim(),
                model = params.model.trim(),
                year = params.year,
                fuelType = params.fuelType,
                mileage = params.mileage,
                licensePlate = params.licensePlate?.trim()?.takeIf { it.isNotEmpty() },
                isActive = params.isActive,
            )
        )
    }
}
