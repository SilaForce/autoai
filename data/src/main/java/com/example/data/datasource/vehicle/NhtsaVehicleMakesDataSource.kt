package com.example.data.datasource.vehicle

import com.example.data.datasource.remote.util.safeHttpCall
import com.example.data.model.vehicle.NhtsaMakesResponseDto
import com.example.data.model.vehicle.NhtsaModelsResponseDto
import com.example.domain.model.app.AppResult
import com.example.domain.datasource.VehicleMakesDataSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NhtsaVehicleMakesDataSource(
    private val client: HttpClient,
) : VehicleMakesDataSource {

    // AtomicReference so concurrent reads see a consistent snapshot.
    private val cachedMakes = AtomicReference<List<String>?>(null)
    // ConcurrentHashMap handles structural mutation racing with reads.
    private val cachedModels = ConcurrentHashMap<String, List<String>>()
    // Mutex serializes the "check cache → fetch → write cache" critical sections so two
    // concurrent callers don't both miss the cache and fire duplicate HTTP requests.
    private val makesFetchMutex = Mutex()
    private val modelsFetchMutex = Mutex()

    override suspend fun getCarMakes(): AppResult<List<String>> {
        cachedMakes.get()?.let { return AppResult.Success(it) }

        return makesFetchMutex.withLock {
            // Re-check inside the lock — another coroutine may have populated the cache
            // while we were waiting on the mutex.
            cachedMakes.get()?.let { return@withLock AppResult.Success(it) }

            safeHttpCall {
                val response: NhtsaMakesResponseDto = client
                    .get("https://vpic.nhtsa.dot.gov/api/vehicles/GetMakesForVehicleType/car?format=json")
                    .body()
                response.results.map { it.name }.sorted().also { cachedMakes.set(it) }
            }
        }
    }

    override suspend fun getModelsForMake(make: String): AppResult<List<String>> {
        cachedModels[make]?.let { return AppResult.Success(it) }

        return modelsFetchMutex.withLock {
            cachedModels[make]?.let { return@withLock AppResult.Success(it) }

            safeHttpCall {
                val response: NhtsaModelsResponseDto = client
                    .get("https://vpic.nhtsa.dot.gov/api/vehicles/GetModelsForMake/$make?format=json")
                    .body()
                response.results.map { it.name }.distinct().sorted()
                    .also { cachedModels[make] = it }
            }
        }
    }
}
