package com.example.data.repository.vehicle

import com.example.data.datasource.remote.util.safeFirebaseCall
import com.example.data.mapper.toVehicle
import com.example.data.mapper.toVehicleDto
import com.example.data.model.vehicle.VehicleDto
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.app.andThen
import com.example.domain.model.vehicle.Vehicle
import com.example.domain.repository.IVehicleRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreVehicleRepository(
    private val firestore: FirebaseFirestore,
) : IVehicleRepository {

    override suspend fun addVehicle(vehicle: Vehicle): AppResult<Vehicle> {
        val documentReference = firestore.collection(VEHICLES_COLLECTION).document()
        val vehicleWithId = vehicle.copy(id = documentReference.id)

        return safeFirebaseCall {
            documentReference
                .set(vehicleWithId.toVehicleDto())
                .await()

            vehicleWithId
        }
    }

    override suspend fun getVehicles(userId: String): AppResult<List<Vehicle>> {
        return safeFirebaseCall {
            firestore.collection(VEHICLES_COLLECTION)
                .whereEqualTo(FIELD_USER_ID, userId)
                .get()
                .await()
        }.andThen { querySnapshot ->
            val vehicles = mutableListOf<Vehicle>()

            for (document in querySnapshot.documents) {
                val dto = document.toObject(VehicleDto::class.java)
                    ?.let { currentDto ->
                        if (currentDto.id.isBlank()) {
                            currentDto.copy(id = document.id)
                        } else {
                            currentDto
                        }
                    }
                    ?: return@andThen AppResult.Failure(DataError.Network.Serialization)

                when (val vehicleResult = dto.toVehicle()) {
                    is AppResult.Success -> vehicles.add(vehicleResult.data)
                    is AppResult.Failure -> return@andThen AppResult.Failure(vehicleResult.error)
                }
            }

            AppResult.Success(vehicles)
        }
    }

    override suspend fun setActiveVehicle(userId: String, vehicleId: String): AppResult<Unit> {
        val vehiclesCollection = firestore.collection(VEHICLES_COLLECTION)

        // Use a single-field query (only userId) to avoid requiring a Firestore composite index.
        return safeFirebaseCall {
            vehiclesCollection
                .whereEqualTo(FIELD_USER_ID, userId)
                .get()
                .await()
        }.andThen { querySnapshot ->
            // Verify the target vehicle exists and belongs to this user
            val targetExists = querySnapshot.documents.any { it.id == vehicleId }
            if (!targetExists) {
                return@andThen AppResult.Failure(DataError.Local.NotFound)
            }

            safeFirebaseCall {
                val batch = firestore.batch()
                for (document in querySnapshot.documents) {
                    batch.update(document.reference, FIELD_IS_ACTIVE, document.id == vehicleId)
                }
                batch.commit().await()
                Unit
            }
        }
    }

    private companion object {
        const val VEHICLES_COLLECTION = "vehicles"
        const val FIELD_USER_ID = "userId"
        const val FIELD_IS_ACTIVE = "isActive"
    }
}
