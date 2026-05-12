package com.example.domain.usecase.user

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.app.andThen
import com.example.domain.model.user.User
import com.example.domain.repository.IAuthRepository
import kotlinx.coroutines.CoroutineDispatcher

data class UpdateUserParams(
    val name: String,
    val username: String,
    val phoneNumber: String,
)

class UpdateUserUseCase(
    private val repository: IAuthRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<UpdateUserParams, User>(dispatcher) {

    override suspend fun execute(params: UpdateUserParams): AppResult<User> {
        if (params.name.isBlank()) {
            return AppResult.Failure(DataError.Local.ValidationError)
        }

        return repository.getCurrentUser().andThen { currentUser ->
            val updatedUser = currentUser.copy(
                name = params.name.trim(),
                username = params.username.trim(),
                phoneNumber = params.phoneNumber.trim(),
            )
            repository.updateUser(updatedUser)
        }
    }
}
