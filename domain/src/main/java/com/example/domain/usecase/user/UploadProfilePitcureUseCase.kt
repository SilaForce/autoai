/* package com.example.domain.usecase.user

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.repository.IStorageRepository
import kotlinx.coroutines.CoroutineDispatcher

data class UploadProfilePictureParams(
    val userId: String,
    val imageBytes: ByteArray
)
class UploadProfilePictureUseCase(
    private val repository: IStorageRepository,
    dispatcher: CoroutineDispatcher
): BaseUseCase<UploadProfilePictureParams, String>(dispatcher) {
    override suspend fun execute(params: UploadProfilePictureParams): AppResult<String> {

        if (params.userId.isBlank() || params.imageBytes.isEmpty() ) {
         return AppResult.Failure(DataError.Local.InvalidInput)
        }

        return repository.uploadProfilePicture(
            userId = params.userId.trim(),
            imageBytes = params.imageBytes
        )
    }
}

 */