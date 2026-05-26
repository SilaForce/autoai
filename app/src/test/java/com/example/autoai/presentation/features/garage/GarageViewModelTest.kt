package com.example.autoai.presentation.features.garage

import androidx.lifecycle.SavedStateHandle
import com.example.autoai.presentation.components.BottomNavItem
import com.example.autoai.presentation.util.UiText
import com.example.autoai.util.MainDispatcherRule
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.app.StartDestination
import com.example.domain.model.cost.Cost
import com.example.domain.model.reminder.Reminder
import com.example.domain.model.user.User
import com.example.domain.model.vehicle.FuelType
import com.example.domain.model.vehicle.Vehicle
import com.example.domain.repository.AuthRepository
import com.example.domain.datasource.CostDataSource
import com.example.domain.datasource.RemindersDataSource
import com.example.domain.datasource.VehicleDataSource
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.DeleteVehicleUseCase
import com.example.domain.usecase.vehicle.GetVehiclesUseCase
import com.example.domain.usecase.vehicle.SetActiveVehicleUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GarageViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init loads vehicles and maps them to ui state`() = runTest {
        val vehicleDataSource = FakeVehicleRepository(
            vehicles = mutableListOf(
                vehicle(id = "vehicle-1", isActive = true),
            )
        )

        val viewModel = createViewModel(vehicleDataSource = vehicleDataSource)
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(BottomNavItem.GARAGE, state.selectedNavItem)
        assertEquals(1, state.vehicles.size)
        assertEquals("Volkswagen Golf", (state.vehicles.first().title as UiText.DynamicString).value)
        assertEquals(true, state.vehicles.first().isActive)
    }

    @Test
    fun `selecting vehicle updates active vehicle and reloads list`() = runTest {
        val vehicleDataSource = FakeVehicleRepository(
            vehicles = mutableListOf(
                vehicle(id = "vehicle-1", isActive = true),
                vehicle(id = "vehicle-2", make = "Audi", model = "A4", isActive = false),
            )
        )

        val viewModel = createViewModel(vehicleDataSource = vehicleDataSource)

        viewModel.onEvent(GarageEvent.OnVehicleSelected("vehicle-2"))

        val state = viewModel.state.value
        assertEquals("vehicle-2", vehicleDataSource.lastSetActiveVehicleId)
        assertFalse(state.isUpdatingActiveVehicle)
        assertTrue(state.vehicles.first { it.id == "vehicle-2" }.isActive)
        assertFalse(state.vehicles.first { it.id == "vehicle-1" }.isActive)
    }

    @Test
    fun `selecting already active vehicle does not trigger repository update`() = runTest {
        val vehicleDataSource = FakeVehicleRepository(
            vehicles = mutableListOf(vehicle(id = "vehicle-1", isActive = true))
        )

        val viewModel = createViewModel(vehicleDataSource = vehicleDataSource)

        viewModel.onEvent(GarageEvent.OnVehicleSelected("vehicle-1"))

        assertEquals(null, vehicleDataSource.lastSetActiveVehicleId)
    }

    @Test
    fun `confirmed delete removes vehicle from list and cascades costs and reminders`() = runTest {
        val vehicleDataSource = FakeVehicleRepository(
            vehicles = mutableListOf(
                vehicle(id = "vehicle-1", isActive = true),
                vehicle(id = "vehicle-2", make = "Audi", model = "A4", isActive = false),
            )
        )
        val costDataSource = FakeCostRepository()
        val reminderDataSource = FakeReminderRepository()

        val viewModel = createViewModel(
            vehicleDataSource = vehicleDataSource,
            costDataSource = costDataSource,
            reminderDataSource = reminderDataSource,
        )

        viewModel.onEvent(GarageEvent.OnDeleteVehicleClicked("vehicle-2"))
        viewModel.onEvent(GarageEvent.OnConfirmDeleteVehicle)

        // Immediately after confirm: vehicle is hidden from visible list, cascade hasn't fired yet.
        val beforeDelay = viewModel.state.value
        assertEquals("vehicle-2", beforeDelay.pendingDeleteId)
        assertEquals(2, beforeDelay.vehicles.size)
        assertEquals(1, beforeDelay.visibleVehicles.size)
        assertEquals("vehicle-1", beforeDelay.visibleVehicles.first().id)
        assertTrue(costDataSource.deletedForVehicleIds.isEmpty())

        // Advance past the undo window — cascade fires.
        advanceTimeBy(5_000)
        runCurrent()

        val afterDelay = viewModel.state.value
        assertEquals(1, afterDelay.vehicles.size)
        assertEquals("vehicle-1", afterDelay.vehicles.first().id)
        assertNull(afterDelay.pendingDeleteId)
        assertEquals(VehicleMenuState.Hidden, afterDelay.menuState)
        assertEquals(listOf("vehicle-2"), costDataSource.deletedForVehicleIds)
        assertEquals(listOf("vehicle-2"), reminderDataSource.deletedForVehicleIds)
    }

    @Test
    fun `undo before window expires cancels the delete`() = runTest {
        val vehicleDataSource = FakeVehicleRepository(
            vehicles = mutableListOf(
                vehicle(id = "vehicle-1", isActive = true),
                vehicle(id = "vehicle-2", make = "Audi", model = "A4", isActive = false),
            )
        )
        val costDataSource = FakeCostRepository()

        val viewModel = createViewModel(
            vehicleDataSource = vehicleDataSource,
            costDataSource = costDataSource,
        )

        viewModel.onEvent(GarageEvent.OnDeleteVehicleClicked("vehicle-2"))
        viewModel.onEvent(GarageEvent.OnConfirmDeleteVehicle)
        viewModel.onEvent(GarageEvent.OnUndoDelete)

        // Let any pending coroutines finish — none should actually delete.
        advanceTimeBy(10_000)
        runCurrent()

        val state = viewModel.state.value
        assertEquals(2, state.vehicles.size)
        assertNull(state.pendingDeleteId)
        assertTrue(costDataSource.deletedForVehicleIds.isEmpty())
    }

    @Test
    fun `deleting last vehicle does not call setActiveVehicle`() = runTest {
        val vehicleDataSource = FakeVehicleRepository(
            vehicles = mutableListOf(vehicle(id = "vehicle-1", isActive = true))
        )

        val viewModel = createViewModel(vehicleDataSource = vehicleDataSource)

        viewModel.onEvent(GarageEvent.OnDeleteVehicleClicked("vehicle-1"))
        viewModel.onEvent(GarageEvent.OnConfirmDeleteVehicle)
        advanceTimeBy(5_000)
        runCurrent()

        assertTrue(viewModel.state.value.vehicles.isEmpty())
        assertNull(vehicleDataSource.lastSetActiveVehicleId)
    }

    @Test
    fun `delete failure leaves vehicles untouched`() = runTest {
        val vehicleDataSource = FakeVehicleRepository(
            vehicles = mutableListOf(
                vehicle(id = "vehicle-1", isActive = true),
                vehicle(id = "vehicle-2", make = "Audi", model = "A4", isActive = false),
            ),
            deleteResult = AppResult.Failure(DataError.Local.NotFound),
        )

        val viewModel = createViewModel(vehicleDataSource = vehicleDataSource)

        viewModel.onEvent(GarageEvent.OnDeleteVehicleClicked("vehicle-2"))
        viewModel.onEvent(GarageEvent.OnConfirmDeleteVehicle)
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(2, viewModel.state.value.vehicles.size)
        assertNull(viewModel.state.value.pendingDeleteId)
        assertEquals(VehicleMenuState.Hidden, viewModel.state.value.menuState)
    }

    @Test
    fun `long-press then edit click clears menu`() = runTest {
        val vehicleDataSource = FakeVehicleRepository(
            vehicles = mutableListOf(vehicle(id = "vehicle-1", isActive = true))
        )

        val viewModel = createViewModel(vehicleDataSource = vehicleDataSource)

        viewModel.onEvent(GarageEvent.OnVehicleLongPressed("vehicle-1"))
        assertEquals(VehicleMenuState.ShowingMenu("vehicle-1"), viewModel.state.value.menuState)

        viewModel.onEvent(GarageEvent.OnEditVehicleClicked("vehicle-1"))
        assertEquals(VehicleMenuState.Hidden, viewModel.state.value.menuState)
    }

    private fun vehicle(
        id: String,
        userId: String = "user-1",
        make: String = "Volkswagen",
        model: String = "Golf",
        year: Int = 2020,
        fuelType: FuelType = FuelType.DIZEL,
        licensePlate: String = "A12-M-345",
        isActive: Boolean = false,
    ) = Vehicle(
        id = id,
        userId = userId,
        make = make,
        model = model,
        year = year,
        fuelType = fuelType,
        licensePlate = licensePlate,
        isActive = isActive,
    )

    private fun createViewModel(
        vehicleDataSource: FakeVehicleRepository,
        costDataSource: CostDataSource = FakeCostRepository(),
        reminderDataSource: RemindersDataSource = FakeReminderRepository(),
    ): GarageViewModel {
        return GarageViewModel(
            savedStateHandle = SavedStateHandle(),
            getCurrentUserUseCase = GetCurrentUserUseCase(
                repository = FakeAuthRepository(),
                dispatcher = mainDispatcherRule.dispatcher,
            ),
            getVehiclesUseCase = GetVehiclesUseCase(
                repository = vehicleDataSource,
                dispatcher = mainDispatcherRule.dispatcher,
            ),
            setActiveVehicleUseCase = SetActiveVehicleUseCase(
                repository = vehicleDataSource,
                dispatcher = mainDispatcherRule.dispatcher,
            ),
            deleteVehicleUseCase = DeleteVehicleUseCase(
                vehicleDataSource = vehicleDataSource,
                costDataSource = costDataSource,
                reminderDataSource = reminderDataSource,
                dispatcher = mainDispatcherRule.dispatcher,
            ),
        )
    }

    private class FakeAuthRepository : AuthRepository {
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

        override suspend fun updateUser(user: User): AppResult<User> = throw NotImplementedError()
        override suspend fun deleteUser(): AppResult<Unit> = throw NotImplementedError()
        override suspend fun logout(): AppResult<Unit> = throw NotImplementedError()
    }

    private class FakeVehicleRepository(
        private val vehicles: MutableList<Vehicle> = mutableListOf(),
        private val deleteResult: AppResult<Unit> = AppResult.Success(Unit),
    ) : VehicleDataSource {
        var lastSetActiveVehicleId: String? = null

        override suspend fun addVehicle(vehicle: Vehicle): AppResult<Vehicle> {
            vehicles.add(vehicle)
            return AppResult.Success(vehicle)
        }

        override suspend fun getVehicles(userId: String): AppResult<List<Vehicle>> {
            return AppResult.Success(vehicles.filter { it.userId == userId })
        }

        override suspend fun getVehicleById(vehicleId: String): AppResult<Vehicle> {
            return vehicles.firstOrNull { it.id == vehicleId }
                ?.let { AppResult.Success(it) }
                ?: AppResult.Failure(DataError.Local.NotFound)
        }

        override suspend fun updateVehicle(vehicle: Vehicle): AppResult<Vehicle> {
            val index = vehicles.indexOfFirst { it.id == vehicle.id }
            if (index == -1) return AppResult.Failure(DataError.Local.NotFound)
            vehicles[index] = vehicle
            return AppResult.Success(vehicle)
        }

        override suspend fun deleteVehicle(userId: String, vehicleId: String): AppResult<Unit> {
            if (deleteResult is AppResult.Failure) return deleteResult
            vehicles.removeAll { it.id == vehicleId && it.userId == userId }
            return AppResult.Success(Unit)
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

        override fun observeActiveVehicle(userId: String): Flow<AppResult<Vehicle?>> =
            flowOf(AppResult.Success(vehicles.firstOrNull { it.userId == userId && it.isActive }))

        override suspend fun deleteAllForUser(userId: String): AppResult<Unit> {
            vehicles.removeAll { it.userId == userId }
            return AppResult.Success(Unit)
        }
    }

    private class FakeCostRepository : CostDataSource {
        val deletedForVehicleIds = mutableListOf<String>()

        override suspend fun addCost(cost: Cost): AppResult<Cost> = throw NotImplementedError()
        override suspend fun getCosts(vehicleId: String): AppResult<List<Cost>> = AppResult.Success(emptyList())
        override suspend fun getCostsByUserId(userId: String): AppResult<List<Cost>> = AppResult.Success(emptyList())
        override suspend fun updateCost(cost: Cost): AppResult<Cost> = throw NotImplementedError()
        override suspend fun deleteCost(costId: String): AppResult<Unit> = throw NotImplementedError()
        override suspend fun deleteCostsForVehicle(vehicleId: String): AppResult<Unit> {
            deletedForVehicleIds += vehicleId
            return AppResult.Success(Unit)
        }

        override suspend fun deleteAllForUser(userId: String): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeReminderRepository : RemindersDataSource {
        val deletedForVehicleIds = mutableListOf<String>()

        override suspend fun addReminder(reminder: Reminder): AppResult<Reminder> = throw NotImplementedError()
        override suspend fun getReminders(vehicleId: String): AppResult<List<Reminder>> = AppResult.Success(emptyList())
        override suspend fun updateReminder(reminder: Reminder): AppResult<Unit> = throw NotImplementedError()
        override suspend fun deleteReminder(reminderId: String): AppResult<Unit> = throw NotImplementedError()
        override suspend fun deleteRemindersForVehicle(vehicleId: String): AppResult<Unit> {
            deletedForVehicleIds += vehicleId
            return AppResult.Success(Unit)
        }
        override suspend fun deleteAllForUser(userId: String): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun getActiveRemindersForVehicle(vehicleId: String): AppResult<List<Reminder>> = AppResult.Success(emptyList())
    }
}
