package com.example.data.repository.vehicle

import com.example.data.datasource.remote.util.safeHttpCall
import com.example.data.model.vehicle.NhtsaMakesResponseDto
import com.example.data.model.vehicle.NhtsaModelsResponseDto
import com.example.domain.model.app.AppResult
import com.example.domain.repository.IVehicleMakesRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class NhtsaVehicleMakesRepository(
    private val client: HttpClient
): IVehicleMakesRepository {

    private var cachedMakes: List<String>? = null
    private val cachedModels = mutableMapOf<String, List<String>>()

    override suspend fun getCarMakes(): AppResult<List<String>> {
        cachedMakes?.let {return AppResult.Success(it) }

        return safeHttpCall {
            val response: NhtsaMakesResponseDto = client
                .get("https://vpic.nhtsa.dot.gov/api/vehicles/GetMakesForVehicleType/car?format=json")
                .body()
            response.results.map { it.name }.sorted().also { cachedMakes = it }
        }
    }

    override suspend fun getModelsForMake(make: String): AppResult<List<String>> {
        cachedModels[make]?.let { return AppResult.Success(it) }

        return safeHttpCall {
            val response: NhtsaModelsResponseDto = client
                .get("https://vpic.nhtsa.dot.gov/api/vehicles/GetModelsForMake/$make?format=json")
                .body()
            response.results.map { it.name }.distinct().sorted().also { cachedModels[make] = it }
        }
    }
}