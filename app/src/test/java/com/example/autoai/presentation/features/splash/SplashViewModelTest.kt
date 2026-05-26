package com.example.autoai.presentation.features.splash

import com.example.autoai.util.MainDispatcherRule
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.StartDestination
import com.example.domain.model.user.User
import com.example.domain.repository.AuthRepository
import com.example.domain.datasource.PreferencesDataSource
import com.example.domain.usecase.session.CheckSessionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init resolves home destination when session exists`() = runTest {
        val viewModel = createViewModel(
            authRepository = FakeAuthRepository(startDestination = StartDestination.Home),
        )

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(StartDestination.Home, state.startDestination)
    }

    @Test
    fun `init falls back to auth when session check throws and onboarding incomplete`() = runTest {
        val viewModel = createViewModel(
            authRepository = ThrowingAuthRepository(),
            preferencesDataSource = FakeDataStorePreferencesDataSource(onboardingCompleted = false),
        )

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(StartDestination.Auth, state.startDestination)
    }

    @Test
    fun `init routes to Login when no session but onboarding completed`() = runTest {
        val viewModel = createViewModel(
            authRepository = FakeAuthRepository(startDestination = StartDestination.Auth),
            preferencesDataSource = FakeDataStorePreferencesDataSource(onboardingCompleted = true),
        )

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(StartDestination.Login, state.startDestination)
    }

    private fun createViewModel(
        authRepository: AuthRepository,
        preferencesDataSource: PreferencesDataSource = FakeDataStorePreferencesDataSource(),
    ): SplashViewModel {
        return SplashViewModel(
            checkSessionUseCase = CheckSessionUseCase(
                repository = authRepository,
                dispatcher = mainDispatcherRule.dispatcher,
            ),
            preferencesDataSource = preferencesDataSource,
        )
    }

    private class FakeAuthRepository(
        private val startDestination: StartDestination,
    ) : AuthRepository {

        override suspend fun checkSession(): StartDestination = startDestination

        override suspend fun register(
            name: String,
            email: String,
            password: String,
        ): AppResult<User> = throw NotImplementedError()

        override suspend fun login(email: String, password: String): AppResult<User> =
            throw NotImplementedError()

        override suspend fun getCurrentUser(): AppResult<User> = throw NotImplementedError()

        override suspend fun updateUser(user: User): AppResult<User> = throw NotImplementedError()
        override suspend fun deleteUser(): AppResult<Unit> = throw NotImplementedError()
        override suspend fun logout(): AppResult<Unit> = throw NotImplementedError()
    }

    private class ThrowingAuthRepository : AuthRepository {

        override suspend fun checkSession(): StartDestination {
            error("Session lookup failed")
        }

        override suspend fun register(
            name: String,
            email: String,
            password: String,
        ): AppResult<User> = throw NotImplementedError()

        override suspend fun login(email: String, password: String): AppResult<User> =
            throw NotImplementedError()

        override suspend fun getCurrentUser(): AppResult<User> = throw NotImplementedError()

        override suspend fun updateUser(user: User): AppResult<User> = throw NotImplementedError()
        override suspend fun deleteUser(): AppResult<Unit> = throw NotImplementedError()
        override suspend fun logout(): AppResult<Unit> = throw NotImplementedError()
    }

    private class FakeDataStorePreferencesDataSource(
        private val onboardingCompleted: Boolean = false,
    ) : PreferencesDataSource {
        override val isNotificationsEnabled: Flow<Boolean> = flowOf(true)
        override val isDarkModeEnabled: Flow<Boolean> = flowOf(false)
        override val isAiAutoRemindersEnabled: Flow<Boolean> = flowOf(false)
        override val isOnboardingCompleted: Flow<Boolean> = flowOf(onboardingCompleted)
        override suspend fun setNotificationsEnabled(enabled: Boolean) = Unit
        override suspend fun setDarkModeEnabled(enabled: Boolean) = Unit
        override suspend fun setAiAutoRemindersEnabled(enabled: Boolean) = Unit
        override suspend fun setOnboardingCompleted(completed: Boolean) = Unit
    }
}
