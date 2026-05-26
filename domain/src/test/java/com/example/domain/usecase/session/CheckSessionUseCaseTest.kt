package com.example.domain.usecase.session

import com.example.domain.model.app.AppResult
import com.example.domain.model.app.StartDestination
import com.example.domain.model.user.User
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckSessionUseCaseTest {

    @Test
    fun `returns home when repository resolves active session`() = runBlocking {
        val useCase = CheckSessionUseCase(
            repository = FakeAuthRepository(startDestination = StartDestination.Home),
            dispatcher = kotlinx.coroutines.Dispatchers.Unconfined,
        )

        val result = useCase()

        assertEquals(StartDestination.Home, result)
    }

    @Test
    fun `returns auth when repository resolves no active session`() = runBlocking {
        val useCase = CheckSessionUseCase(
            repository = FakeAuthRepository(startDestination = StartDestination.Auth),
            dispatcher = kotlinx.coroutines.Dispatchers.Unconfined,
        )

        val result = useCase()

        assertEquals(StartDestination.Auth, result)
    }

    private class FakeAuthRepository(
        private val startDestination: StartDestination,
    ) : AuthRepository {

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

        override suspend fun updateUser(user: User): AppResult<User> {
            throw NotImplementedError()
        }

        override suspend fun deleteUser(): AppResult<Unit> {
            throw NotImplementedError()
        }

        override suspend fun logout(): AppResult<Unit> {
            throw NotImplementedError()
        }
    }
}
