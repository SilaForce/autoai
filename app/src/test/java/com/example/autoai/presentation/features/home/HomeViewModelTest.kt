package com.example.autoai.presentation.features.home

import com.example.autoai.navigation.AppNavigator
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loads active vehicle into home state`() = runTest {
        val viewModel = createViewModel(
            vehicleRepository = FakeVehicleRepository(
                vehicles = listOf(
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
        )

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertTrue(state.hasActiveVehicle)
        assertEquals("Volkswagen Golf", state.vehicleName)
        assertEquals("A12-M-345", state.vehiclePlate)
    }

    @Test
    fun `keeps empty active vehicle state when no active vehicle exists`() = runTest {
        val viewModel = createViewModel(
            vehicleRepository = FakeVehicleRepository(
                vehicles = listOf(
                    Vehicle(
                        id = "vehicle-1",
                        userId = "user-1",
                        make = "Volkswagen",
                        model = "Golf",
                        year = 2020,
                        fuelType = FuelType.DIZEL,
                        licensePlate = "A12-M-345",
                        isActive = false,
                    )
                )
            )
        )

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertFalse(state.hasActiveVehicle)
        assertEquals("", state.vehicleName)
        assertEquals("", state.vehiclePlate)
    }

    private fun createViewModel(
        vehicleRepository: FakeVehicleRepository,
    ): HomeViewModel {
        return HomeViewModel(
            getCurrentUserUseCase = GetCurrentUserUseCase(
                repository = FakeAuthRepository(),
                dispatcher = mainDispatcherRule.dispatcher,
            ),
            getVehiclesUseCase = GetVehiclesUseCase(
                repository = vehicleRepository,
                dispatcher = mainDispatcherRule.dispatcher,
            ),
            navigator = AppNavigator(),
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
        private val vehicles: List<Vehicle>,
    ) : IVehicleRepository {
        override suspend fun addVehicle(vehicle: Vehicle): AppResult<Vehicle> {
            throw NotImplementedError()
        }

        override suspend fun getVehicles(userId: String): AppResult<List<Vehicle>> {
            return AppResult.Success(vehicles.filter { it.userId == userId })
        }

        override suspend fun setActiveVehicle(userId: String, vehicleId: String): AppResult<Unit> {
            throw NotImplementedError()
        }
    }
}
