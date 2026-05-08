package com.example.domain.model.app

sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Failure(val error: DataError) : AppResult<Nothing>
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

inline fun <T> AppResult<T>.onFailure(action: (DataError) -> Unit): AppResult<T> {
    if (this is AppResult.Failure) action(error)
    return this
}

fun <T> AppResult<T>.logError(tag: String = "AutoAIDebug"): AppResult<T> {
    if (this is AppResult.Failure) {
        println("[$tag] Error Details: ${this.error}")
    }
    return this
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> {
    return when (this) {
        is AppResult.Success -> AppResult.Success(transform(data))
        is AppResult.Failure -> AppResult.Failure(error)
    }
}

suspend inline fun <T, R> AppResult<T>.andThen(
    transform: suspend (T) -> AppResult<R>
): AppResult<R> {
    return when (this) {
        is AppResult.Success -> transform(data)
        is AppResult.Failure -> AppResult.Failure(error)
    }
}

fun <T> AppResult<T>.asEmptyResult(): AppResult<Unit> {
    return map { }
}
