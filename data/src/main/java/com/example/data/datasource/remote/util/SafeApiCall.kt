package com.example.data.datasource.remote.util

import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.google.ai.client.generativeai.type.GoogleGenerativeAIException
import com.google.ai.client.generativeai.type.InvalidAPIKeyException
import com.google.ai.client.generativeai.type.PromptBlockedException
import com.google.ai.client.generativeai.type.QuotaExceededException
import com.google.ai.client.generativeai.type.RequestTimeoutException
import com.google.ai.client.generativeai.type.ResponseStoppedException
import com.google.ai.client.generativeai.type.SerializationException
import com.google.ai.client.generativeai.type.ServerException
import com.google.ai.client.generativeai.type.UnsupportedUserLocationException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException

suspend fun <T> safeFirebaseCall(
    execute: suspend () -> T
): AppResult<T> {
    return try {
        val result = execute()
        AppResult.Success(result)
    } catch (_: FirebaseNetworkException) {
        AppResult.Failure(DataError.Network.NoInternet)
    } catch (_: FirebaseAuthInvalidUserException) {
        // Email not found / user disabled
        AppResult.Failure(DataError.Network.Unauthorized)
    } catch (_: FirebaseAuthInvalidCredentialsException) {
        // Wrong password or malformed credential
        AppResult.Failure(DataError.Network.Unauthorized)
    } catch (_: FirebaseAuthUserCollisionException) {
        // Email already in use (registration)
        AppResult.Failure(DataError.Network.ServerError)
    } catch (e: FirebaseFirestoreException) {
        when (e.code) {
            FirebaseFirestoreException.Code.NOT_FOUND -> AppResult.Failure(DataError.Local.NotFound)
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> AppResult.Failure(DataError.Local.PermissionDenied)
            FirebaseFirestoreException.Code.UNAVAILABLE -> AppResult.Failure(DataError.Network.NoInternet)
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> AppResult.Failure(DataError.Network.Timeout)
            else -> AppResult.Failure(DataError.Network.ServerError)
        }
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        AppResult.Failure(DataError.Network.Unknown)
    }
}

suspend fun <T> safeGenerativeAiCall(
    execute: suspend () -> T
): AppResult<T> {
    return try {
        val result = execute()
        AppResult.Success(result)
    } catch (e: InvalidAPIKeyException) {
        logGenerativeAiFailure(e)
        AppResult.Failure(DataError.Network.Unauthorized)
    } catch (e: QuotaExceededException) {
        logGenerativeAiFailure(e)
        AppResult.Failure(DataError.Network.Http(429))
    } catch (e: RequestTimeoutException) {
        logGenerativeAiFailure(e)
        AppResult.Failure(DataError.Network.Timeout)
    } catch (e: SerializationException) {
        logGenerativeAiFailure(e)
        AppResult.Failure(DataError.Network.Serialization)
    } catch (e: UnsupportedUserLocationException) {
        logGenerativeAiFailure(e)
        AppResult.Failure(DataError.Network.Http(403))
    } catch (e: PromptBlockedException) {
        logGenerativeAiFailure(e)
        AppResult.Failure(DataError.Network.ServerError)
    } catch (e: ResponseStoppedException) {
        logGenerativeAiFailure(e)
        AppResult.Failure(DataError.Network.ServerError)
    } catch (e: ServerException) {
        logGenerativeAiFailure(e)
        mapToNetworkError(e.message)
    } catch (e: GoogleGenerativeAIException) {
        logGenerativeAiFailure(e)
        mapToNetworkError(e.message)
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        logGenerativeAiFailure(e)
        mapToNetworkError(e.message)
    }
}

private fun mapToNetworkError(message: String?): AppResult.Failure {
    val code = message?.let { HTTP_CODE_REGEX.find(it)?.groupValues?.getOrNull(1)?.toIntOrNull() }
    return when (code) {
        401, 403 -> AppResult.Failure(DataError.Network.Unauthorized)
        408 -> AppResult.Failure(DataError.Network.Timeout)
        429 -> AppResult.Failure(DataError.Network.Http(429))
        in 400..599 -> AppResult.Failure(DataError.Network.Http(code!!))
        else -> AppResult.Failure(DataError.Network.Unknown)
    }
}

private fun logGenerativeAiFailure(throwable: Throwable) {
    println(
        "[GeminiChatRepository] ${throwable::class.qualifiedName}: ${throwable.message}" +
            (throwable.cause?.let { " | cause=${it::class.qualifiedName}: ${it.message}" } ?: "")
    )
}

private val HTTP_CODE_REGEX = Regex("\"code\"\\s*:\\s*(\\d{3})")

