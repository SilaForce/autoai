package com.example.domain.usecase.user

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher

class LogoutUseCase (
    private val repository: AuthRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<Unit, Unit>(dispatcher) {

    override suspend fun execute(params: Unit): AppResult<Unit> {
        return repository.logout()
    }
}