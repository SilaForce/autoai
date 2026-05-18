package com.example.data.datasource.remote.util

import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import kotlinx.coroutines.CancellationException
import java.io.IOException
suspend fun <T> safeHttpCall(block: suspend () -> T): AppResult<T> {
    return try {
        AppResult.Success(block())
        } catch (e: IOException){
        AppResult.Failure(DataError.Network.NoInternet)
        } catch (e: CancellationException){
        throw e
        } catch (e: Exception){
        AppResult.Failure(DataError.Network.Unknown)
        }
}