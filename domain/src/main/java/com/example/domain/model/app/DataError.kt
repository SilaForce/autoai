package com.example.domain.model.app

sealed interface DataError {
    sealed interface Network : DataError {
        data object NoInternet : Network
        data object Timeout : Network
        data object Serialization : Network
        data object ServerError : Network
        data object Unauthorized : Network
        data class Http(val code: Int) : Network
        data object Unknown : Network
    }

    sealed interface Local : DataError {
        data object DiskFull : Local
        data object PermissionDenied : Local
        data object NotFound : Local
        data object Unknown : Local

        sealed interface Validation : Local {
            data object FieldEmpty : Validation
            data object InvalidEmail : Validation
            data object InvalidPassword : Validation
            data object PasswordsDoNotMatch : Validation
            data object InvalidDate : Validation
            data object DateInPast : Validation
            data object InvalidAmount : Validation
            data object InvalidMileage : Validation
            data object InvalidYear : Validation
            data object InvalidName : Validation
            data object FuelTypeNotSelected : Validation
            data object Generic : Validation
        }
    }
}
