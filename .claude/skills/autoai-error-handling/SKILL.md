---
name: autoai-error-handling
description: AutoAI's typed-error pattern — Result wrapper, DataError, safeFirebaseCall, and UiText mapping. Use this skill whenever the user is handling errors, returning a Result from a repository, catching Firebase exceptions, mapping errors to UiText for the UI, or designing error flows. Trigger on any mention of error handling, Result, DataError, AuthError, safeCall, exceptions, try/catch, UiText error mapping, or "what should this return when it fails".
---

# AutoAI Error Handling

## Approach

On hard problems in this area, think step by step and consider a few approaches before picking one. Walk through the reasoning explicitly — surface the tradeoffs, then commit to the choice. Skipping this on non-trivial work tends to produce solutions that look right but miss a constraint.

## Core Principles

- **No Exceptions for Business Logic:** Never throw exceptions for expected failures (wrong password, no internet). Use the `Result` wrapper.
- **Typed Errors:** Every failure must be represented by a typed error implementing the `Error` interface.
- **Layered Responsibility:** Raw exceptions (Firebase, Firestore, etc.) **must** be caught in the **Data Layer** and mapped to `DataError` before reaching the Domain or Presentation layers.

## Result Wrapper (`core:domain`)

```kotlin
interface Error

sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : com.example.Error>(val error: E) : Result<Nothing, E>
}

typealias EmptyResult<E> = Result<Unit, E>
```

Included extensions: `.map()`, `.onSuccess()`, `.onFailure()`, `.asEmptyResult()`.

## Shared Error Types (`core:domain`)

```kotlin
sealed interface DataError : Error {
    enum class Remote : DataError {
        SERVICE_UNAVAILABLE, UNAUTHORIZED, NOT_FOUND,
        NO_INTERNET, SERVER_ERROR, UNKNOWN
    }
    enum class Local : DataError {
        DISK_FULL, NOT_FOUND, UNKNOWN
    }
}
```

## Firebase Safe Call Helper (`core:data`)

```kotlin
suspend fun <T> safeFirebaseCall(block: suspend () -> T): Result<T, DataError.Remote> {
    return try {
        Result.Success(block())
    } catch (e: FirebaseAuthException) {
        Result.Error(DataError.Remote.UNAUTHORIZED)
    } catch (e: FirebaseFirestoreException) {
        Result.Error(DataError.Remote.SERVER_ERROR)
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Result.Error(DataError.Remote.UNKNOWN)
    }
}
```

**Critical:** Always rethrow `CancellationException` to keep coroutines working correctly.

## UiText & Mapping (`core:presentation`)

Never hardcode error strings in ViewModels. All errors must be mapped to string resources using `UiText`.

```kotlin
fun DataError.toUiText(): UiText {
    return when (this) {
        DataError.Remote.NO_INTERNET -> UiText.StringResource(R.string.error_no_internet)
        DataError.Remote.UNAUTHORIZED -> UiText.StringResource(R.string.error_auth_failed)
        else -> UiText.StringResource(R.string.error_unknown)
    }
}
```

Usage in ViewModel:

```kotlin
_state.update { it.copy(error = error.toUiText()) }
```

## Exception Handling Philosophy

| Exception origin | Catch in | Example |
|---|---|---|
| Firebase / Firestore | Data layer | Map to `DataError.Remote` |
| Auth validation | Domain layer | Map to `AuthError` (e.g., `INVALID_EMAIL`) |
| Business logic | Domain layer | Return `Result.Error` with custom Enum |

## Checklist: Error Handling

- [ ] Catch raw Firebase/framework exceptions in the **Data Layer**
- [ ] Ensure all repository functions return `Result<T, E>`
- [ ] Create a `.toUiText()` extension for every user-facing error
- [ ] Verify that ViewModels only deal with `UiText` and never raw Strings for errors
- [ ] Ensure `CancellationException` is rethrown in safe call blocks
