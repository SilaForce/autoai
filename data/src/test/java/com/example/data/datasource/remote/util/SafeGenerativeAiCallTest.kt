package com.example.data.datasource.remote.util

import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.google.ai.client.generativeai.type.InvalidAPIKeyException
import com.google.ai.client.generativeai.type.QuotaExceededException
import com.google.ai.client.generativeai.type.RequestTimeoutException
import com.google.ai.client.generativeai.type.SerializationException
import com.google.ai.client.generativeai.type.ServerException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeGenerativeAiCallTest {

    @Test
    fun `returns success when generative ai block completes normally`() = runBlocking {
        val result = safeGenerativeAiCall { "ok" }

        assertTrue(result is AppResult.Success)
        assertEquals("ok", (result as AppResult.Success).data)
    }

    @Test
    fun `maps invalid api key to unauthorized`() = runBlocking {
        val result = safeGenerativeAiCall<String> {
            throw InvalidAPIKeyException("invalid key")
        }

        assertTrue(result is AppResult.Failure)
        assertEquals(DataError.Network.Unauthorized, (result as AppResult.Failure).error)
    }

    @Test
    fun `maps exhausted quota to http 429`() = runBlocking {
        val result = safeGenerativeAiCall<String> {
            throw QuotaExceededException("quota exceeded")
        }

        assertTrue(result is AppResult.Failure)
        assertEquals(DataError.Network.Http(429), (result as AppResult.Failure).error)
    }

    @Test
    fun `maps timeout to timeout error`() = runBlocking {
        val result = safeGenerativeAiCall<String> {
            throw RequestTimeoutException("too slow")
        }

        assertTrue(result is AppResult.Failure)
        assertEquals(DataError.Network.Timeout, (result as AppResult.Failure).error)
    }

    @Test
    fun `maps serialization problem to serialization error`() = runBlocking {
        val result = safeGenerativeAiCall<String> {
            throw SerializationException("bad payload")
        }

        assertTrue(result is AppResult.Failure)
        assertEquals(DataError.Network.Serialization, (result as AppResult.Failure).error)
    }

    @Test
    fun `maps server 404 to http 404`() = runBlocking {
        val result = safeGenerativeAiCall<String> {
            throw ServerException("""
                {
                  "error": {
                    "code": 404,
                    "message": "model not found",
                    "status": "NOT_FOUND"
                  }
                }
            """.trimIndent())
        }

        assertTrue(result is AppResult.Failure)
        assertEquals(DataError.Network.Http(404), (result as AppResult.Failure).error)
    }

    @Test
    fun `rethrows cancellation exception`() {
        try {
            runBlocking {
                safeGenerativeAiCall<String> {
                    throw CancellationException("Cancelled")
                }
            }
        } catch (exception: CancellationException) {
            assertEquals("Cancelled", exception.message)
            return
        }

        throw AssertionError("Expected CancellationException to be rethrown")
    }
}

