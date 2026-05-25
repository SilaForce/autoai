package com.example.autoai.presentation.features.home

import com.example.autoai.navigation.AppNavigator
import com.example.autoai.util.MainDispatcherRule
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.StartDestination
import com.example.domain.model.cost.Cost
import com.example.domain.model.reminder.Reminder
import com.example.domain.model.user.User
import com.example.domain.model.vehicle.FuelType
import com.example.domain.model.vehicle.Vehicle
import com.example.domain.repository.IAuthRepository
import com.example.domain.repository.ICostRepository
import com.example.domain.repository.IRemindersRepository
import com.example.domain.repository.IVehicleRepository
import com.example.domain.usecase.cost.GetCostStatisticsUseCase
import com.example.domain.usecase.reminder.GetRemindersUseCase
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.ObserveActiveVehicleUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
        val activeVehicle = Vehicle(
            id = "vehicle-1",
            userId = "user-1",
            make = "Volkswagen",
            model = "Golf",
            year = 2020,
            fuelType = FuelType.DIZEL,
            licensePlate = "A12-M-345",
            isActive = true,
        )
        val viewModel = createViewModel(vehicleRepository = FakeVehicleRepository(activeVehicle))

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertTrue(state.hasActiveVehicle)
        assertEquals("Volkswagen Golf", state.activeVehicleName)
        assertEquals("A12-M-345", state.activeVehiclePlate)
    }

    @Test
    fun `keeps empty active vehicle state when no active vehicle exists`() = runTest {
        val viewModel = createViewModel(vehicleRepository = FakeVehicleRepository(activeVehicle = null))

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertFalse(state.hasActiveVehicle)
        assertEquals("", state.activeVehicleName)
        assertEquals("", state.activeVehiclePlate)
    }

    private fun createViewModel(
        vehicleRepository: FakeVehicleRepository,
    ): HomeViewModel {
        return HomeViewModel(
            getCurrentUserUseCase = GetCurrentUserUseCase(
                repository = FakeAuthRepository(),
                dispatcher = mainDispatcherRule.dispatcher,
            ),
            observeActiveVehicleUseCase = ObserveActiveVehicleUseCase(
                repository = vehicleRepository,
            ),
            getCostStatisticsUseCase = GetCostStatisticsUseCase(
                repository = FakeCostRepository(),
                dispatcher = mainDispatcherRule.dispatcher,
            ),
            getReminderUseCase = GetRemindersUseCase(
                repository = FakeReminderRepository(),
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
        ): AppResult<User> = throw NotImplementedError()

        override suspend fun login(email: String, password: String): AppResult<User> =
            throw NotImplementedError()

        override suspend fun getCurrentUser(): AppResult<User> {
            return AppResult.Success(
                User(
                    id = "user-1",
                    email = "amar@example.com",
                    name = "Amar",
                )
            )
        }

        override suspend fun updateUser(user: User): AppResult<User> = throw NotImplementedError()
        override suspend fun deleteUser(): AppResult<Unit> = throw NotImplementedError()
        override suspend fun logout(): AppResult<Unit> = throw NotImplementedError()
    }

    private class FakeVehicleRepository(
        private val activeVehicle: Vehicle?,
    ) : IVehicleRepository {
        override suspend fun addVehicle(vehicle: Vehicle): AppResult<Vehicle> =
            throw NotImplementedError()

        override suspend fun getVehicles(userId: String): AppResult<List<Vehicle>> =
            throw NotImplementedError()

        override suspend fun getVehicleById(vehicleId: String): AppResult<Vehicle> =
            throw NotImplementedError()

        override suspend fun updateVehicle(vehicle: Vehicle): AppResult<Vehicle> =
            throw NotImplementedError()

        override suspend fun deleteVehicle(userId: String, vehicleId: String): AppResult<Unit> =
            throw NotImplementedError()

        override suspend fun setActiveVehicle(userId: String, vehicleId: String): AppResult<Unit> =
            throw NotImplementedError()

        override fun observeActiveVehicle(userId: String): Flow<AppResult<Vehicle?>> =
            flowOf(AppResult.Success(activeVehicle))

        override suspend fun deleteAllForUser(userId: String): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeCostRepository : ICostRepository {
        override suspend fun addCost(cost: Cost): AppResult<Cost> = throw NotImplementedError()
        override suspend fun getCosts(vehicleId: String): AppResult<List<Cost>> =
            AppResult.Success(emptyList())
        override suspend fun getCostsByUserId(userId: String): AppResult<List<Cost>> =
            AppResult.Success(emptyList())
        override suspend fun updateCost(cost: Cost): AppResult<Cost> = throw NotImplementedError()
        override suspend fun deleteCost(costId: String): AppResult<Unit> = throw NotImplementedError()
        override suspend fun deleteCostsForVehicle(vehicleId: String): AppResult<Unit> =
            AppResult.Success(Unit)
        override suspend fun deleteAllForUser(userId: String): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeReminderRepository : IRemindersRepository {
        override suspend fun addReminder(reminder: Reminder): AppResult<Reminder> =
            throw NotImplementedError()
        override suspend fun getReminders(vehicleId: String): AppResult<List<Reminder>> =
            AppResult.Success(emptyList())
        override suspend fun updateReminder(reminder: Reminder): AppResult<Unit> =
            throw NotImplementedError()
        override suspend fun deleteReminder(reminderId: String): AppResult<Unit> =
            throw NotImplementedError()
        override suspend fun deleteRemindersForVehicle(vehicleId: String): AppResult<Unit> =
            AppResult.Success(Unit)
        override suspend fun deleteAllForUser(userId: String): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun getActiveRemindersForVehicle(vehicleId: String): AppResult<List<Reminder>> =
            AppResult.Success(emptyList())
    }
}
