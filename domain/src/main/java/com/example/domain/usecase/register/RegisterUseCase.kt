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

        if (!ValidationUtil.isValidFullName(params.name) ||
            !ValidationUtil.isValidEmail(params.email) ||
            !ValidationUtil.isValidPassword(params.password)
        ) {
            // Ako BILO ŠTA ne valja, odmah prekini i vrati grešku!
            return AppResult.Failure(DataError.Local.ValidationError)
        }

        return repository.register(
            name = params.name,
            email = params.email,
            password = params.password
        )
    }
}