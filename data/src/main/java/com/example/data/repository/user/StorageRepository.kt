/* package com.example.data.repository.user

import com.example.data.datasource.remote.util.safeFirebaseCall
import com.example.domain.model.app.AppResult
import com.example.domain.repository.IStorageRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageRepository (
    private val FirebaseStorage: FirebaseStorage
) : IStorageRepository {
    override suspend fun uploadProfilePicture(userId: String, imageBytes: ByteArray): AppResult<String> {
        return safeFirebaseCall {

            val storageRef = FirebaseStorage.reference
                .child(PROFILE_PICTURES_FOLDER)
                .child("$userId.jpg")

            storageRef.putBytes(imageBytes).await()

            val downloadUrl = storageRef.downloadUrl.await()

            downloadUrl.toString()

        }

    }
    private companion object {
        private const val PROFILE_PICTURES_FOLDER = "profile_pictures"
    }
}

    */