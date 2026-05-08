package com.example.autoai.presentation.features.splash

import com.example.autoai.util.MainDispatcherRule
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.StartDestination
import com.example.domain.model.user.User
import com.example.domain.repository.IAuthRepository
import com.example.domain.usecase.session.CheckSessionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        val viewModel = SplashViewModel(
            checkSessionUseCase = CheckSessionUseCase(
                repository = FakeAuthRepository(startDestination = StartDestination.Home),
                dispatcher = mainDispatcherRule.dispatcher,
            )
        )

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(StartDestination.Home, state.startDestination)
    }

    @Test
    fun `init falls back to auth when session check throws`() = runTest {
        val viewModel = SplashViewModel(
            checkSessionUseCase = CheckSessionUseCase(
                repository = ThrowingAuthRepository(),
                dispatcher = mainDispatcherRule.dispatcher,
            )
        )

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(StartDestination.Auth, state.startDestination)
    }

    private class FakeAuthRepository(
        private val startDestination: StartDestination,
    ) : IAuthRepository {

        override suspend fun checkSession(): StartDestination = startDestination

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
            throw NotImplementedError()
        }
    }

    private class ThrowingAuthRepository : IAuthRepository {

        override suspend fun checkSession(): StartDestination {
            error("Session lookup failed")
        }

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
            throw NotImplementedError()
        }
    }
}
