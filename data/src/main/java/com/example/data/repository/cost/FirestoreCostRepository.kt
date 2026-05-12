package com.example.data.repository.cost

import com.example.data.datasource.remote.util.safeFirebaseCall
import com.example.data.mapper.toCost
import com.example.data.mapper.toCostDto
import com.example.data.model.cost.CostDto
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.app.andThen
import com.example.domain.model.cost.Cost
import com.example.domain.repository.ICostRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreCostRepository(
    private val firestore: FirebaseFirestore,
) : ICostRepository {

    override suspend fun addCost(cost: Cost): AppResult<Cost> {
        val documentReference = firestore.collection(COSTS_COLLECTION).document()
        val costWithId = cost.copy(id = documentReference.id)

        return safeFirebaseCall {
            documentReference
                .set(costWithId.toCostDto())
                .await()

            costWithId
        }
    }

    override suspend fun getCosts(vehicleId: String): AppResult<List<Cost>> {
        // NOTE: This query requires a Firestore composite index on (vehicleId ASC, dateMillis DESC).
        // When you first run this query, Logcat will print a direct link to create the index
        // in the Firebase Console — click it once and the index will be built automatically.
        return safeFirebaseCall {
            firestore.collection(COSTS_COLLECTION)
                .whereEqualTo(FIELD_VEHICLE_ID, vehicleId)
                .orderBy(FIELD_DATE_MILLIS, com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
        }.andThen { querySnapshot ->
            val costs = mutableListOf<Cost>()

            for (document in querySnapshot.documents) {
                val dto = document.toObject(CostDto::class.java)
                    ?.let { currentDto ->
                        if (currentDto.id.isBlank()) currentDto.copy(id = document.id)
                        else currentDto
                    }
                    ?: return@andThen AppResult.Failure(DataError.Network.Serialization)

                when (val costResult = dto.toCost()) {
                    is AppResult.Success -> costs.add(costResult.data)
                    is AppResult.Failure -> return@andThen AppResult.Failure(costResult.error)
                }
            }

            AppResult.Success(costs)
        }
    }

    override suspend fun getCostsByUserId(userId: String): AppResult<List<Cost>> {
        return safeFirebaseCall {
            firestore.collection(COSTS_COLLECTION)
                .whereEqualTo(FIELD_USER_ID, userId)
                .get()
                .await()
        }.andThen { querySnapshot ->
            val costs = mutableListOf<Cost>()

            for (document in querySnapshot.documents) {
                val dto = document.toObject(CostDto::class.java)
                    ?.let { currentDto ->
                        if (currentDto.id.isBlank()) currentDto.copy(id = document.id)
                        else currentDto
                    }
                    ?: return@andThen AppResult.Failure(DataError.Network.Serialization)

                when (val costResult = dto.toCost()) {
                    is AppResult.Success -> costs.add(costResult.data)
                    is AppResult.Failure -> return@andThen AppResult.Failure(costResult.error)
                }
            }

            AppResult.Success(costs)
        }
    }

    private companion object {
        const val COSTS_COLLECTION = "costs"
        const val FIELD_VEHICLE_ID = "vehicleId"
        const val FIELD_USER_ID = "userId"
        const val FIELD_DATE_MILLIS = "dateMillis"
    }
}
