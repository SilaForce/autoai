package com.example.data.mapper

import com.example.data.model.vehicle.VehicleDto
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.vehicle.FuelType
import com.example.domain.model.vehicle.Vehicle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleMapperTest {

    @Test
    fun `toVehicleDto maps fuel type as enum name string`() {
        val vehicle = Vehicle(
            id = "vehicle-1",
            userId = "user-1",
            make = "Volkswagen",
            model = "Golf",
            year = 2020,
            fuelType = FuelType.DIZEL,
            mileage = 123000,
            licensePlate = "A12-J-345",
            isActive = true,
        )

        val dto = vehicle.toVehicleDto()

        assertEquals("vehicle-1", dto.id)
        assertEquals("DIZEL", dto.fuelType)
        assertEquals("user-1", dto.userId)
        assertEquals(true, dto.isActive)
    }

    @Test
    fun `toVehicle maps valid dto to domain model`() {
        val dto = VehicleDto(
            id = "vehicle-1",
            userId = "user-1",
            make = "Volkswagen",
            model = "Golf",
            year = 2020,
            fuelType = "BENZIN",
            mileage = 95000,
            licensePlate = "K23-M-111",
            isActive = false,
        )

        val result = dto.toVehicle()

        assertTrue(result is AppResult.Success)
        val vehicle = (result as AppResult.Success).data
        assertEquals(FuelType.BENZIN, vehicle.fuelType)
        assertEquals("vehicle-1", vehicle.id)
        assertEquals("user-1", vehicle.userId)
        assertEquals(95000, vehicle.mileage)
    }

    @Test
    fun `toVehicle returns serialization error for invalid fuel type`() {
        val dto = VehicleDto(
            id = "vehicle-1",
            userId = "user-1",
            make = "Volkswagen",
            model = "Golf",
            year = 2020,
            fuelType = "HYDROGEN",
        )

        val result = dto.toVehicle()

        assertTrue(result is AppResult.Failure)
        assertEquals(DataError.Network.Serialization, (result as AppResult.Failure).error)
    }
}
