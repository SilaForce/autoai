package com.example.domain.usecase.user

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.repository.IAuthRepository
import kotlinx.coroutines.CoroutineDispatcher

class DeleteUserUseCase(
    private val repository: IAuthRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<Unit, Unit>(dispatcher) {

    override suspend fun execute(params: Unit): AppResult<Unit> {
        return repository.deleteUser()
    }
}
