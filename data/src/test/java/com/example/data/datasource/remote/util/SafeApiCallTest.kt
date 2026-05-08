package com.example.data.datasource.remote.util

import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeApiCallTest {

    @Test
    fun `returns success when block completes normally`() = runBlocking {
        val result = safeFirebaseCall { "ok" }

        assertTrue(result is AppResult.Success)
        assertEquals("ok", (result as AppResult.Success).data)
    }

    @Test
    fun `maps unexpected exception to unknown network error`() = runBlocking {
        val result = safeFirebaseCall<String> {
            throw IllegalStateException("Unexpected")
        }

        assertTrue(result is AppResult.Failure)
        assertEquals(DataError.Network.Unknown, (result as AppResult.Failure).error)
    }

    @Test
    fun `rethrows cancellation exception`() {
        try {
            runBlocking {
                safeFirebaseCall<String> {
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


