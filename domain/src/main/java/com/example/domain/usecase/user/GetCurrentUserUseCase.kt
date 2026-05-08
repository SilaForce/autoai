package com.example.domain.usecase.user

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.user.User
import com.example.domain.repository.IAuthRepository
import kotlinx.coroutines.CoroutineDispatcher

class GetCurrentUserUseCase(
    private val repository: IAuthRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<Unit, User>(dispatcher) {

    override suspend fun execute(params: Unit): AppResult<User> {
        return repository.getCurrentUser()
    }
}

