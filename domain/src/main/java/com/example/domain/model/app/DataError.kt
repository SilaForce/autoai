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
        data object InvalidInput : Local
        data object PermissionDenied : Local
        data object NotFound : Local
        data object Unknown : Local

        data object ValidationError: Local
    }
}