package com.example.autoai.presentation.features.garage.add

import androidx.lifecycle.SavedStateHandle
import com.example.autoai.R
import com.example.autoai.presentation.util.UiText
import com.example.autoai.util.MainDispatcherRule
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.StartDestination
import com.example.domain.model.user.User
import com.example.domain.model.vehicle.FuelType
import com.example.domain.model.vehicle.Vehicle
import com.example.domain.repository.IAuthRepository
import com.example.domain.repository.IVehicleMakesRepository
import com.example.domain.repository.IVehicleRepository
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.vehicle.AddVehicleUseCase
import com.example.domain.usecase.vehicle.GetCarMakesUseCase
import com.example.domain.usecase.vehicle.GetModelsForMakeUseCase
import com.example.domain.usecase.vehicle.GetVehicleByIdUseCase
import com.example.domain.usecase.vehicle.UpdateVehicleUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddVehicleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `selecting fuel type updates selection state`() {
        val viewModel = createViewModel()

        viewModel.onEvent(AddVehicleEvent.OnFuelTypeSelected(FuelType.DIZEL))

        assertEquals(FuelType.DIZEL, viewModel.state.value.selectedFuelType)
    }

    @Test
    fun `save with invalid year emits error side effect`() = runTest {
        val viewModel = createViewModel()
        val sideEffect = async { viewModel.sideEffects.first() }

        viewModel.onEvent(AddVehicleEvent.OnMakeChanged("Volkswagen"))
        viewModel.onEvent(AddVehicleEvent.OnModelChanged("Golf"))
        viewModel.onEvent(AddVehicleEvent.OnFuelTypeSelected(FuelType.DIZEL))
        viewModel.onEvent(AddVehicleEvent.OnSaveClicked)

        val result = sideEffect.await()
        assertEquals(
            AddVehicleSideEffect.ShowError(UiText.StringResource(R.string.add_vehicle_error_invalid_year)),
            result,
        )
    }

    @Test
    fun `save with valid form persists vehicle and navigates back`() = runTest {
        val repository = FakeVehicleRepository()
        val viewModel = createViewModel(repository)
        val sideEffect = async { viewModel.sideEffects.first() }

        viewModel.onEvent(AddVehicleEvent.OnMakeChanged("Volkswagen"))
        viewModel.onEvent(AddVehicleEvent.OnModelChanged("Golf"))
        viewModel.onEvent(AddVehicleEvent.OnYearSelected(2020))
        viewModel.onEvent(AddVehicleEvent.OnFuelTypeSelected(FuelType.DIZEL))
        viewModel.onEvent(AddVehicleEvent.OnMileageChanged("123000"))
        viewModel.onEvent(AddVehicleEvent.OnLicensePlateChanged("A12-M-345"))
        viewModel.onEvent(AddVehicleEvent.OnSaveClicked)

        val result = sideEffect.await()
        assertEquals(AddVehicleSideEffect.NavigateBack, result)
        assertEquals("Volkswagen", repository.addedVehicle?.make)
        assertEquals(FuelType.DIZEL, repository.addedVehicle?.fuelType)
        assertEquals(123000, repository.addedVehicle?.mileage)
    }

    @Test
    fun `back on dirty form shows discard dialog instead of navigating immediately`() {
        val viewModel = createViewModel()

        viewModel.onEvent(AddVehicleEvent.OnMakeChanged("Volkswagen"))
        viewModel.onEvent(AddVehicleEvent.OnBackClicked)

        assertTrue(viewModel.state.value.isFormDirty)
        assertTrue(viewModel.state.value.showDiscardDialog)
    }

    private fun createViewModel(
        repository: FakeVehicleRepository = FakeVehicleRepository(),
    ): AddVehicleViewModel {
        val makesRepository = FakeMakesRepository()
        return AddVehicleViewModel(
            savedStateHandle = SavedStateHandle(),
            getCurrentUserUseCase = GetCurrentUserUseCase(
                repository = FakeAuthRepository(),
                dispatcher = mainDispatcherRule.dispatcher,
            ),
            addVehicleUseCase = AddVehicleUseCase(
                repository = repository,
                dispatcher = mainDispatcherRule.dispatcher,
            ),
            updateVehicleUseCase = UpdateVehicleUseCase(
                repository = repository,
                dispatcher = mainDispatcherRule.dispatcher,
            ),
            getVehicleByIdUseCase = GetVehicleByIdUseCase(
                repository = repository,
                dispatcher = mainDispatcherRule.dispatcher,
            ),
            getCarMakesUseCase = GetCarMakesUseCase(
                repository = makesRepository,
                dispatcher = mainDispatcherRule.dispatcher,
            ),
            getModelsForMakeUseCase = GetModelsForMakeUseCase(
                repository = makesRepository,
                dispatcher = mainDispatcherRule.dispatcher,
            ),
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

    private class FakeVehicleRepository : IVehicleRepository {
        var addedVehicle: Vehicle? = null

        override suspend fun addVehicle(vehicle: Vehicle): AppResult<Vehicle> {
            addedVehicle = vehicle
            return AppResult.Success(vehicle.copy(id = "vehicle-1"))
        }

        override suspend fun getVehicles(userId: String): AppResult<List<Vehicle>> =
            throw NotImplementedError()

        override suspend fun getVehicleById(vehicleId: String): AppResult<Vehicle> =
            throw NotImplementedError()

        override suspend fun updateVehicle(vehicle: Vehicle): AppResult<Vehicle> =
            AppResult.Success(vehicle)

        override suspend fun deleteVehicle(userId: String, vehicleId: String): AppResult<Unit> =
            throw NotImplementedError()

        override suspend fun setActiveVehicle(userId: String, vehicleId: String): AppResult<Unit> =
            throw NotImplementedError()

        override fun observeActiveVehicle(userId: String): Flow<AppResult<Vehicle?>> = emptyFlow()

        override suspend fun deleteAllForUser(userId: String): AppResult<Unit> =
            throw NotImplementedError()
    }

    private class FakeMakesRepository : IVehicleMakesRepository {
        override suspend fun getCarMakes(): AppResult<List<String>> =
            AppResult.Success(listOf("Volkswagen", "Audi", "BMW"))

        override suspend fun getModelsForMake(make: String): AppResult<List<String>> =
            AppResult.Success(listOf("Golf", "Polo", "Passat"))
    }
}
