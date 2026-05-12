package com.example.domain.repository

import com.example.domain.model.app.AppResult

interface IStorageRepository {
    suspend fun uploadProfilePicture(userId: String, imageBytes: ByteArray): AppResult<String>
}