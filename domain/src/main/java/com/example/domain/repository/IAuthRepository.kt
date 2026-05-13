package com.example.domain.repository

import com.example.domain.model.app.AppResult
import com.example.domain.model.app.StartDestination
import com.example.domain.model.user.User

interface IAuthRepository {
    suspend fun checkSession(): StartDestination
    suspend fun register(name: String, email: String, password: String): AppResult<User>
    suspend fun login(email: String, password: String): AppResult<User>
    suspend fun getCurrentUser(): AppResult<User>
    suspend fun updateUser(user: User): AppResult<User>
    suspend fun deleteUser(): AppResult<Unit>
    suspend fun logout(): AppResult<Unit>
}