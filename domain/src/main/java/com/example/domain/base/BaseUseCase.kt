package com.example.domain.base

import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import kotlinx.coroutines.CancellationException
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
                // Rethrow CancellationException so structured concurrency works.
                // Without this, a ViewModel clear / rotation surfaces a fake "Unknown" failure.
                if (e is CancellationException) throw e
                AppResult.Failure(DataError.Network.Unknown)
            }
        }
    }

    protected abstract suspend fun execute(params: P): AppResult<R>
}