package com.example.domain.usecase.vehicle

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.datasource.VehicleMakesDataSource
import kotlinx.coroutines.CoroutineDispatcher

class GetCarMakesUseCase (
    private val repository: VehicleMakesDataSource,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<Unit, List<String>>(dispatcher) {

    override suspend fun execute(params: Unit): AppResult<List<String>> {
       return repository.getCarMakes()
    }

}