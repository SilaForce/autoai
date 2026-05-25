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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreVehicleRepository(
    private val firestore: FirebaseFirestore,
) : IVehicleRepository {

    override suspend fun addVehicle(vehicle: Vehicle): AppResult<Vehicle> {
        val documentReference = firestore.collection(VEHICLES_COLLECTION).document()

        return safeFirebaseCall {
            // First-time-user shortcut: if the user has no existing vehicles, mark this
            // one active so HomeViewModel doesn't land them on a "no active vehicle"
            // empty state until they manually tap the only car they own.
            val isFirstVehicle = firestore.collection(VEHICLES_COLLECTION)
                .whereEqualTo(FIELD_USER_ID, vehicle.userId)
                .limit(1)
                .get()
                .await()
                .isEmpty

            val vehicleWithId = vehicle.copy(
                id = documentReference.id,
                isActive = vehicle.isActive || isFirstVehicle,
            )

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
                .orderBy(FIELD_MAKE, Query.Direction.ASCENDING)
                .orderBy(FIELD_MODEL, Query.Direction.ASCENDING)
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

    override suspend fun getVehicleById(vehicleId: String): AppResult<Vehicle> {
        return safeFirebaseCall {
            firestore.collection(VEHICLES_COLLECTION).document(vehicleId).get().await()
        }.andThen { document ->
            val dto = document.toObject(VehicleDto::class.java)
                ?.let { if (it.id.isBlank()) it.copy(id = document.id) else it }
                ?: return@andThen AppResult.Failure(DataError.Network.Serialization)
            dto.toVehicle()
        }
    }

    override suspend fun updateVehicle(vehicle: Vehicle): AppResult<Vehicle> {
        return safeFirebaseCall {
            // merge() so server-side or future-schema fields aren't nulled out by a full overwrite.
            firestore.collection(VEHICLES_COLLECTION).document(vehicle.id)
                .set(vehicle.toVehicleDto(), SetOptions.merge())
                .await()
            vehicle
        }
    }

    override suspend fun deleteVehicle(userId: String, vehicleId: String): AppResult<Unit> {
        val documentReference = firestore.collection(VEHICLES_COLLECTION).document(vehicleId)
        return safeFirebaseCall {
            documentReference.get().await()
        }.andThen { document ->
            val ownerId = document.getString(FIELD_USER_ID)
            if (!document.exists() || ownerId != userId) {
                AppResult.Failure(DataError.Local.NotFound)
            } else {
                safeFirebaseCall {
                    documentReference.delete().await()
                    Unit
                }
            }
        }
    }

    override suspend fun setActiveVehicle(userId: String, vehicleId: String): AppResult<Unit> {
        val vehiclesCollection = firestore.collection(VEHICLES_COLLECTION)

        // Two-phase to get optimistic concurrency control:
        //  1. Outside the transaction: query the user's vehicles to discover their ids.
        //     (Firestore transactions only support `txn.get(documentRef)`, not compound
        //     queries — and the body must be synchronous, no suspension.)
        //  2. Inside the transaction: `txn.get(ref)` each known vehicle so the writes
        //     participate in OCC. If any read doc changes between read and commit, the
        //     transaction retries automatically (up to 5 times by default).
        //
        // Residual race: a vehicle added by another client after the step-1 query won't
        // be in the step-2 lock set. For this app (single-user, single-device typical),
        // the risk is acceptable. The proper fix would be to denormalize `activeVehicleId`
        // onto the user document so the active flag becomes a single field update.
        return safeFirebaseCall {
            vehiclesCollection
                .whereEqualTo(FIELD_USER_ID, userId)
                .get()
                .await()
        }.andThen { querySnapshot ->
            val targetExists = querySnapshot.documents.any { it.id == vehicleId }
            if (!targetExists) {
                return@andThen AppResult.Failure(DataError.Local.NotFound)
            }

            val knownIds = querySnapshot.documents.map { it.id }
            safeFirebaseCall {
                firestore.runTransaction { txn ->
                    // Firestore transactions require ALL reads to happen before ANY writes.
                    // Interleaving them throws IllegalStateException at runtime when the
                    // collection has more than one doc. So: read all refs first to mark
                    // them for OCC, then flip isActive on each in a second pass.
                    val refs = knownIds.map { id -> vehiclesCollection.document(id) }
                    for (ref in refs) {
                        // Return value unused; the read just locks the doc for this txn.
                        txn.get(ref)
                    }
                    for (ref in refs) {
                        txn.update(ref, FIELD_IS_ACTIVE, ref.id == vehicleId)
                    }
                    null
                }.await()
                Unit
            }
        }
    }

    override suspend fun deleteAllForUser(userId: String): AppResult<Unit> {
        return safeFirebaseCall {
            firestore.collection(VEHICLES_COLLECTION)
                .whereEqualTo(FIELD_USER_ID, userId)
                .get()
                .await()
        }.andThen { querySnapshot ->
            safeFirebaseCall {
                querySnapshot.documents.chunked(BATCH_LIMIT).forEach { chunk ->
                    val batch = firestore.batch()
                    chunk.forEach { batch.delete(it.reference) }
                    batch.commit().await()
                }
                Unit
            }
        }
    }

    override fun observeActiveVehicle(userId: String): Flow<AppResult<Vehicle?>> = callbackFlow {
        val registration = firestore.collection(VEHICLES_COLLECTION)
            .whereEqualTo(FIELD_USER_ID, userId)
            .whereEqualTo(FIELD_IS_ACTIVE, true)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(AppResult.Failure(DataError.Network.Unknown))
                    return@addSnapshotListener
                }
                val document = snapshot?.documents?.firstOrNull()
                if (document == null) {
                    trySend(AppResult.Success(null))
                    return@addSnapshotListener
                }
                val dto = document.toObject(VehicleDto::class.java)
                    ?.let { if (it.id.isBlank()) it.copy(id = document.id) else it }
                if (dto == null) {
                    trySend(AppResult.Failure(DataError.Network.Serialization))
                    return@addSnapshotListener
                }
                trySend(dto.toVehicle().let { result ->
                    when (result) {
                        is AppResult.Success -> AppResult.Success(result.data)
                        is AppResult.Failure -> AppResult.Failure(result.error)
                    }
                })
            }
        awaitClose { registration.remove() }
    }

    private companion object {
        const val VEHICLES_COLLECTION = "vehicles"
        const val FIELD_USER_ID = "userId"
        const val FIELD_IS_ACTIVE = "isActive"
        const val FIELD_MAKE = "make"
        const val FIELD_MODEL = "model"
        const val BATCH_LIMIT = 500
    }
}
