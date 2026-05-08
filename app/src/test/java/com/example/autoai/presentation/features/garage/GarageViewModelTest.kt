package com.example.autoai.presentation.features.garage

import com.example.autoai.navigation.AppNavigator
import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.util.UiText
import com.example.autoai.util.MainDispatcherRule
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.StartDestination
import com.example.domain.model.user.User
import com.example.domain.model.vehicle.FuelType
import com.example.domain.model.vehicle.Vehicle
import com.example.domain.repository.IAuthRepository
import com.example.domain.repository.IVehicleRepository
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.GetVehiclesUseCase
import com.example.domain.usecase.vehicle.SetActiveVehicleUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GarageViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init loads vehicles and maps them to ui state`() = runTest {
        val vehicleRepository = FakeVehicleRepository(
            vehicles = mutableListOf(
                Vehicle(
                    id = "vehicle-1",
                    userId = "user-1",
                    make = "Volkswagen",
                    model = "Golf",
                    year = 2020,
                    fuelType = FuelType.DIZEL,
                    licensePlate = "A12-M-345",
                    isActive = true,
                )
            )
        )

        val viewModel = createViewModel(vehicleRepository = vehicleRepository)
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(BottomNavItem.GARAGE, state.selectedNavItem)
        assertEquals(1, state.vehicles.size)
        assertEquals("Volkswagen Golf", (state.vehicles.first().title as UiText.DynamicString).value)
        assertEquals(true, state.vehicles.first().isActive)
    }

    @Test
    fun `selecting vehicle updates active vehicle and reloads list`() = runTest {
        val vehicleRepository = FakeVehicleRepository(
            vehicles = mutableListOf(
                Vehicle(
                    id = "vehicle-1",
                    userId = "user-1",
                    make = "Volkswagen",
                    model = "Golf",
                    year = 2020,
                    fuelType = FuelType.DIZEL,
                    licensePlate = "A12-M-345",
                    isActive = true,
                ),
                Vehicle(
                    id = "vehicle-2",
                    userId = "user-1",
                    make = "Audi",
                    model = "A4",
                    year = 2021,
                    fuelType = FuelType.BENZIN,
                    licensePlate = "K23-J-111",
                    isActive = false,
                )
            )
        )

        val viewModel = createViewModel(vehicleRepository = vehicleRepository)

        viewModel.onEvent(GarageEvent.OnVehicleSelected("vehicle-2"))

        val state = viewModel.state.value
        assertEquals("vehicle-2", vehicleRepository.lastSetActiveVehicleId)
        assertFalse(state.isUpdatingActiveVehicle)
        assertTrue(state.vehicles.first { it.id == "vehicle-2" }.isActive)
        assertFalse(state.vehicles.first { it.id == "vehicle-1" }.isActive)
    }

    @Test
    fun `selecting already active vehicle does not trigger repository update`() = runTest {
        val vehicleRepository = FakeVehicleRepository(
            vehicles = mutableListOf(
                Vehicle(
                    id = "vehicle-1",
                    userId = "user-1",
                    make = "Volkswagen",
                    model = "Golf",
                    year = 2020,
                    fuelType = FuelType.DIZEL,
                    licensePlate = "A12-M-345",
                    isActive = true,
                )
            )
        )

        val viewModel = createViewModel(vehicleRepository = vehicleRepository)

        viewModel.onEvent(GarageEvent.OnVehicleSelected("vehicle-1"))

        assertEquals(null, vehicleRepository.lastSetActiveVehicleId)
    }

    private fun createViewModel(
        vehicleRepository: FakeVehicleRepository,
        navigator: AppNavigator = AppNavigator(),
    ): GarageViewModel {
        return GarageViewModel(
            getCurrentUserUseCase = GetCurrentUserUseCase(
                repository = FakeAuthRepository(),
                dispatcher = mainDispatcherRule.dispatcher,
            ),
            getVehiclesUseCase = GetVehiclesUseCase(
                repository = vehicleRepository,
                dispatcher = mainDispatcherRule.dispatcher,
            ),
            setActiveVehicleUseCase = SetActiveVehicleUseCase(
                repository = vehicleRepository,
                dispatcher = mainDispatcherRule.dispatcher,
            ),
            navigator = navigator,
        )
    }

    private class FakeAuthRepository : IAuthRepository {
        override suspend fun checkSession(): StartDestination = StartDestination.Home

        override suspend fun register(
            name: String,
            email: String,
            password: String,
        ): AppResult<User> {
            throw NotImplementedError()
        }

        override suspend fun login(email: String, password: String): AppResult<User> {
            throw NotImplementedError()
        }

        override suspend fun getCurrentUser(): AppResult<User> {
            return AppResult.Success(
                User(
                    id = "user-1",
                    email = "amar@example.com",
                    name = "Amar",
                )
            )
        }
    }

    private class FakeVehicleRepository(
        private val vehicles: MutableList<Vehicle> = mutableListOf(),
    ) : IVehicleRepository {
        var lastSetActiveVehicleId: String? = null

        override suspend fun addVehicle(vehicle: Vehicle): AppResult<Vehicle> {
            vehicles.add(vehicle)
            return AppResult.Success(vehicle)
        }

        override suspend fun getVehicles(userId: String): AppResult<List<Vehicle>> {
            return AppResult.Success(vehicles.filter { it.userId == userId })
        }

        override suspend fun setActiveVehicle(userId: String, vehicleId: String): AppResult<Unit> {
            lastSetActiveVehicleId = vehicleId
            val updatedVehicles = vehicles.map { vehicle ->
                if (vehicle.userId == userId) {
                    vehicle.copy(isActive = vehicle.id == vehicleId)
                } else {
                    vehicle
                }
            }
            vehicles.clear()
            vehicles.addAll(updatedVehicles)
            return AppResult.Success(Unit)
        }
    }
}

