package com.example.domain.usecase.vehicle

import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.vehicle.FuelType
import com.example.domain.model.vehicle.Vehicle
import com.example.domain.repository.IVehicleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleUseCasesTest {

    @Test
    fun `add vehicle returns validation error when make is blank`() = runBlocking {
        val repository = FakeVehicleRepository()
        val useCase = AddVehicleUseCase(repository, Dispatchers.Unconfined)

        val result = useCase(
            AddVehicleParams(
                userId = "user-1",
                make = "   ",
                model = "Golf",
                year = 2020,
                fuelType = FuelType.DIZEL,
            )
        )

        assertTrue(result is AppResult.Failure)
        assertEquals(DataError.Local.Validation.FieldEmpty, (result as AppResult.Failure).error)
        assertEquals(null, repository.addedVehicle)
    }

    @Test
    fun `add vehicle trims values and delegates to repository`() = runBlocking {
        val repository = FakeVehicleRepository()
        val useCase = AddVehicleUseCase(repository, Dispatchers.Unconfined)

        val result = useCase(
            AddVehicleParams(
                userId = "  user-1  ",
                make = "  Volkswagen ",
                model = " Golf  ",
                year = 2020,
                fuelType = FuelType.DIZEL,
                mileage = 120000,
                licensePlate = "  A12-J-345  ",
                isActive = true,
            )
        )

        assertTrue(result is AppResult.Success)
        val addedVehicle = repository.addedVehicle
        requireNotNull(addedVehicle)
        assertEquals("", addedVehicle.id)
        assertEquals("user-1", addedVehicle.userId)
        assertEquals("Volkswagen", addedVehicle.make)
        assertEquals("Golf", addedVehicle.model)
        assertEquals("A12-J-345", addedVehicle.licensePlate)
        assertTrue(addedVehicle.isActive)
    }

    @Test
    fun `get vehicles returns validation error when user id is blank`() = runBlocking {
        val repository = FakeVehicleRepository()
        val useCase = GetVehiclesUseCase(repository, Dispatchers.Unconfined)

        val result = useCase(GetVehiclesParams(userId = "   "))

        assertTrue(result is AppResult.Failure)
        assertEquals(DataError.Local.Validation.Generic, (result as AppResult.Failure).error)
    }

    @Test
    fun `get vehicles delegates trimmed user id to repository`() = runBlocking {
        val repository = FakeVehicleRepository()
        val useCase = GetVehiclesUseCase(repository, Dispatchers.Unconfined)

        val result = useCase(GetVehiclesParams(userId = "  user-42  "))

        assertTrue(result is AppResult.Success)
        assertEquals("user-42", repository.lastGetVehiclesUserId)
    }

    @Test
    fun `set active vehicle returns validation error when vehicle id is blank`() = runBlocking {
        val repository = FakeVehicleRepository()
        val useCase = SetActiveVehicleUseCase(repository, Dispatchers.Unconfined)

        val result = useCase(
            SetActiveVehicleParams(
                userId = "user-1",
                vehicleId = "   ",
            )
        )

        assertTrue(result is AppResult.Failure)
        assertEquals(DataError.Local.Validation.Generic, (result as AppResult.Failure).error)
    }

    @Test
    fun `set active vehicle delegates trimmed identifiers to repository`() = runBlocking {
        val repository = FakeVehicleRepository()
        val useCase = SetActiveVehicleUseCase(repository, Dispatchers.Unconfined)

        val result = useCase(
            SetActiveVehicleParams(
                userId = "  user-7 ",
                vehicleId = " vehicle-7  ",
            )
        )

        assertTrue(result is AppResult.Success)
        assertEquals("user-7", repository.lastSetActiveUserId)
        assertEquals("vehicle-7", repository.lastSetActiveVehicleId)
    }

    private class FakeVehicleRepository : IVehicleRepository {
        var addedVehicle: Vehicle? = null
        var lastGetVehiclesUserId: String? = null
        var lastSetActiveUserId: String? = null
        var lastSetActiveVehicleId: String? = null

        override suspend fun addVehicle(vehicle: Vehicle): AppResult<Vehicle> {
            addedVehicle = vehicle
            return AppResult.Success(vehicle.copy(id = "vehicle-1"))
        }

        override suspend fun getVehicles(userId: String): AppResult<List<Vehicle>> {
            lastGetVehiclesUserId = userId
            return AppResult.Success(emptyList())
        }

        override suspend fun setActiveVehicle(userId: String, vehicleId: String): AppResult<Unit> {
            lastSetActiveUserId = userId
            lastSetActiveVehicleId = vehicleId
            return AppResult.Success(Unit)
        }
    }
}
