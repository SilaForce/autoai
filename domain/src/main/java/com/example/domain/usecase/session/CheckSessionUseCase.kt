package com.example.domain.usecase.session

import com.example.domain.model.app.StartDestination
import com.example.domain.repository.IAuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class CheckSessionUseCase(
    private val repository: IAuthRepository,
    private val dispatcher: CoroutineDispatcher,
) {

    suspend operator fun invoke(): StartDestination {
        return withContext(dispatcher) {
            repository.checkSession()
        }
    }
}
