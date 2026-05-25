package com.example.data.datasource.remote.util

import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SafeGenerativeAiCallTest {

    @Test
    fun `returns success when block completes normally`() = runBlocking {
        val result = safeGenerativeAiCall { "ok" }

        assertTrue(result is AppResult.Success)
        assertEquals("ok", (result as AppResult.Success).data)
    }

    @Test
    fun `socket timeout maps to network timeout`() = runBlocking {
        val result = safeGenerativeAiCall<String> { throw SocketTimeoutException("timed out") }

        assertTrue(result is AppResult.Failure)
        assertEquals(DataError.Network.Timeout, (result as AppResult.Failure).error)
    }

    @Test
    fun `unknown host maps to no internet`() = runBlocking {
        val result = safeGenerativeAiCall<String> { throw UnknownHostException("no dns") }

        assertTrue(result is AppResult.Failure)
        assertEquals(DataError.Network.NoInternet, (result as AppResult.Failure).error)
    }

    @Test
    fun `ioexception maps to no internet`() = runBlocking {
        val result = safeGenerativeAiCall<String> { throw IOException("disconnected") }

        assertTrue(result is AppResult.Failure)
        assertEquals(DataError.Network.NoInternet, (result as AppResult.Failure).error)
    }

    @Test
    fun `quota keyword in message maps to rate limit`() = runBlocking {
        val result = safeGenerativeAiCall<String> {
            throw RuntimeException("Quota exceeded for metric: generate_content_free_tier_requests")
        }

        assertTrue(result is AppResult.Failure)
        assertEquals(DataError.Network.Http(429), (result as AppResult.Failure).error)
    }

    @Test
    fun `http 401 in message maps to unauthorized`() = runBlocking {
        val result = safeGenerativeAiCall<String> {
            throw RuntimeException("Got status: 401 unauthenticated")
        }

        assertTrue(result is AppResult.Failure)
        assertEquals(DataError.Network.Unauthorized, (result as AppResult.Failure).error)
    }

    @Test
    fun `unknown exception with no recognized signal maps to unknown`() = runBlocking {
        val result = safeGenerativeAiCall<String> { throw RuntimeException("nothing useful") }

        assertTrue(result is AppResult.Failure)
        assertEquals(DataError.Network.Unknown, (result as AppResult.Failure).error)
    }

    @Test(expected = CancellationException::class)
    fun `cancellation propagates`() = runBlocking {
        safeGenerativeAiCall<String> { throw CancellationException("cancelled") }
        Unit
    }
}
