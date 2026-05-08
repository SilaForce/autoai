package com.example.domain.base

import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

abstract class BaseUseCase<in P, out R>(
    private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(params: P): AppResult<R> {
        return withContext(dispatcher) {
            try {
                execute(params)
            } catch (e: Exception) {
                AppResult.Failure(DataError.Network.Unknown)
            }
        }
    }

    protected abstract suspend fun execute(params: P): AppResult<R>
}