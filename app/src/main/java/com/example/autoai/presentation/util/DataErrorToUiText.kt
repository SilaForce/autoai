package com.example.autoai.presentation.util

import com.example.autoai.R
import com.example.domain.model.app.DataError

fun DataError.asUiText(): UiText = when (this) {
    // Local
    DataError.Local.ValidationError    -> UiText.StringResource(R.string.error_invalid_email)
    DataError.Local.InvalidInput -> UiText.StringResource(R.string.error_invalid_input)
    DataError.Local.NotFound           -> UiText.StringResource(R.string.error_account_not_found)
    DataError.Local.DiskFull,
    DataError.Local.PermissionDenied,
    DataError.Local.Unknown            -> UiText.StringResource(R.string.error_unknown)

    // Network
    DataError.Network.NoInternet       -> UiText.StringResource(R.string.error_no_internet)
    DataError.Network.Timeout          -> UiText.StringResource(R.string.error_timeout)
    DataError.Network.Unauthorized     -> UiText.StringResource(R.string.error_unauthorized)
    DataError.Network.Serialization,
    DataError.Network.ServerError,
    DataError.Network.Unknown          -> UiText.StringResource(R.string.error_unknown)
    is DataError.Network.Http          -> when (code) {
        401  -> UiText.StringResource(R.string.error_unauthorized)
        404  -> UiText.StringResource(R.string.error_account_not_found)
        else -> UiText.StringResource(R.string.error_unknown)
    }
}
