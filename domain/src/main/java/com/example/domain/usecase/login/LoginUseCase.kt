package com.example.domain.usecase.login

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.user.User
import com.example.domain.repository.IAuthRepository
import com.example.domain.util.ValidationUtil
import kotlinx.coroutines.CoroutineDispatcher

data class LoginParams(
    val email: String,
    val password: String,
)

class LoginUseCase(
    private val repository: IAuthRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<LoginParams, User>(dispatcher) {

    override suspend fun execute(params: LoginParams): AppResult<User> {
        if (params.email.isBlank() || params.password.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.FieldEmpty)
        }
        if (!ValidationUtil.isValidEmail(params.email)) {
            return AppResult.Failure(DataError.Local.Validation.InvalidEmail)
        }
        if (!ValidationUtil.isValidPassword(params.password)) {
            return AppResult.Failure(DataError.Local.Validation.InvalidPassword)
        }

        return repository.login(
            email = params.email,
            password = params.password,
        )
    }
}

