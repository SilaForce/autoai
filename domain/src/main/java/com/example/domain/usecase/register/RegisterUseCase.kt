package com.example.domain.usecase.register

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.user.User
import com.example.domain.repository.IAuthRepository
import com.example.domain.util.ValidationUtil
import kotlinx.coroutines.CoroutineDispatcher

data class RegisterParams(
    val name: String,
    val email: String,
    val password: String,
)

class RegisterUseCase(
    private val repository: IAuthRepository,
    dispatcher: CoroutineDispatcher
    ): BaseUseCase <RegisterParams, User>(dispatcher) {

    override suspend fun execute(params: RegisterParams): AppResult<User> {
        if (params.name.isBlank() || params.email.isBlank() || params.password.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.FieldEmpty)
        }
        if (!ValidationUtil.isValidFullName(params.name)) {
            return AppResult.Failure(DataError.Local.Validation.InvalidName)
        }
        if (!ValidationUtil.isValidEmail(params.email)) {
            return AppResult.Failure(DataError.Local.Validation.InvalidEmail)
        }
        if (!ValidationUtil.isValidPassword(params.password)) {
            return AppResult.Failure(DataError.Local.Validation.InvalidPassword)
        }

        return repository.register(
            name = params.name,
            email = params.email,
            password = params.password
        )
    }
}