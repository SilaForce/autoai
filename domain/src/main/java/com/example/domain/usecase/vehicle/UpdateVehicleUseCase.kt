package com.example.domain.usecase.vehicle

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.vehicle.FuelType
import com.example.domain.model.vehicle.Vehicle
import com.example.domain.repository.IVehicleRepository
import com.example.domain.util.ValidationUtil
import kotlinx.coroutines.CoroutineDispatcher

data class UpdateVehicleParams(
    val vehicleId: String,
    val userId: String,
    val make: String,
    val model: String,
    val year: Int,
    val fuelType: FuelType,
    val mileage: Int? = null,
    val licensePlate: String? = null,
    val isActive: Boolean,
)

class UpdateVehicleUseCase(
    private val repository: IVehicleRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<UpdateVehicleParams, Vehicle>(dispatcher) {

    override suspend fun execute(params: UpdateVehicleParams): AppResult<Vehicle> {
        // Blank vehicleId/userId is a programmer error, not form validation; treat as NotFound.
        if (!ValidationUtil.isValidVehicleText(params.vehicleId) ||
            !ValidationUtil.isValidVehicleText(params.userId)
        ) {
            return AppResult.Failure(DataError.Local.NotFound)
        }
        if (!ValidationUtil.isValidVehicleText(params.make) ||
            !ValidationUtil.isValidVehicleText(params.model)
        ) {
            return AppResult.Failure(DataError.Local.Validation.FieldEmpty)
        }
        if (!ValidationUtil.isValidVehicleYear(params.year)) {
            return AppResult.Failure(DataError.Local.Validation.InvalidYear)
        }
        if (!ValidationUtil.isValidMileage(params.mileage)) {
            return AppResult.Failure(DataError.Local.Validation.InvalidMileage)
        }

        return repository.updateVehicle(
            Vehicle(
                id = params.vehicleId.trim(),
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
