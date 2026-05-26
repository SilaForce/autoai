package com.example.data.datasource.cost

import android.util.Log
import com.example.data.datasource.remote.util.safeFirebaseCall
import com.example.data.mapper.toCost
import com.example.data.mapper.toCostDto
import com.example.data.model.cost.CostDto
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.andThen
import com.example.domain.model.cost.Cost
import com.example.domain.datasource.CostDataSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreCostDataSource(
    private val firestore: FirebaseFirestore,
) : CostDataSource {

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

                if (dto == null) {
                    Log.w(TAG, "Skipping malformed cost document: ${document.id}")
                    continue
                }

                when (val costResult = dto.toCost()) {
                    is AppResult.Success -> costs.add(costResult.data)
                    is AppResult.Failure -> {
                        Log.w(TAG, "Skipping cost document with mapping error: ${document.id}")
                    }
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

                if (dto == null) {
                    Log.w(TAG, "Skipping malformed cost document: ${document.id}")
                    continue
                }

                when (val costResult = dto.toCost()) {
                    is AppResult.Success -> costs.add(costResult.data)
                    is AppResult.Failure -> {
                        Log.w(TAG, "Skipping cost document with mapping error: ${document.id}")
                    }
                }
            }

            AppResult.Success(costs)
        }
    }

    override suspend fun updateCost(cost: Cost): AppResult<Cost> {
        val documentReference = firestore.collection(COSTS_COLLECTION).document(cost.id)
        val costWithId = cost.copy(id = documentReference.id)
        return safeFirebaseCall {
            // merge() so server-side or future-schema fields aren't nulled out by a full
            // overwrite. Same fix pattern as FirestoreVehicleDataSource.updateVehicle.
            documentReference
                .set(costWithId.toCostDto(), SetOptions.merge())
                .await()
            costWithId
        }
    }

    override suspend fun deleteCost(costId: String): AppResult<Unit> {
        return safeFirebaseCall {
            firestore.collection(COSTS_COLLECTION).document(costId).delete().await()
        }
    }

    override suspend fun deleteCostsForVehicle(vehicleId: String): AppResult<Unit> {
        return deleteWhere(FIELD_VEHICLE_ID, vehicleId)
    }

    override suspend fun deleteAllForUser(userId: String): AppResult<Unit> {
        return deleteWhere(FIELD_USER_ID, userId)
    }

    private suspend fun deleteWhere(field: String, value: String): AppResult<Unit> {
        return safeFirebaseCall {
            firestore.collection(COSTS_COLLECTION)
                .whereEqualTo(field, value)
                .get()
                .await()
        }.andThen { querySnapshot ->
            safeFirebaseCall {
                // Firestore WriteBatch caps at 500 ops; chunk for safety.
                querySnapshot.documents.chunked(BATCH_LIMIT).forEach { chunk ->
                    val batch = firestore.batch()
                    chunk.forEach { document -> batch.delete(document.reference) }
                    batch.commit().await()
                }
                Unit
            }
        }
    }

    private companion object {
        const val TAG = "FirestoreCostDataSource"
        const val COSTS_COLLECTION = "costs"
        const val FIELD_VEHICLE_ID = "vehicleId"
        const val FIELD_USER_ID = "userId"
        const val FIELD_DATE_MILLIS = "dateMillis"
        const val BATCH_LIMIT = 500
    }
}
