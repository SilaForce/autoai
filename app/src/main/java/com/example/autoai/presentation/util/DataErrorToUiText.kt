package com.example.autoai.presentation.util

import com.example.autoai.R
import com.example.domain.model.app.DataError

fun DataError.asUiText(): UiText = when (this) {
    // Local — Validation
    DataError.Local.Validation.FieldEmpty           -> UiText.StringResource(R.string.error_field_empty)
    DataError.Local.Validation.InvalidEmail         -> UiText.StringResource(R.string.error_invalid_email)
    DataError.Local.Validation.InvalidPassword      -> UiText.StringResource(R.string.error_invalid_password)
    DataError.Local.Validation.PasswordsDoNotMatch  -> UiText.StringResource(R.string.error_passwords_do_not_match)
    DataError.Local.Validation.InvalidDate          -> UiText.StringResource(R.string.error_invalid_date)
    DataError.Local.Validation.DateInPast           -> UiText.StringResource(R.string.error_date_in_past)
    DataError.Local.Validation.InvalidAmount        -> UiText.StringResource(R.string.error_invalid_amount)
    DataError.Local.Validation.InvalidMileage       -> UiText.StringResource(R.string.error_invalid_mileage)
    DataError.Local.Validation.InvalidYear          -> UiText.StringResource(R.string.error_invalid_year)
    DataError.Local.Validation.InvalidName          -> UiText.StringResource(R.string.error_invalid_name)
    DataError.Local.Validation.FuelTypeNotSelected  -> UiText.StringResource(R.string.error_fuel_type_not_selected)
    DataError.Local.Validation.Generic              -> UiText.StringResource(R.string.error_invalid_input)

    // Local — Other
    DataError.Local.NotFound                        -> UiText.StringResource(R.string.error_account_not_found)
    DataError.Local.DiskFull,
    DataError.Local.PermissionDenied,
    DataError.Local.Unknown                         -> UiText.StringResource(R.string.error_unknown)

    // Network
    DataError.Network.NoInternet                    -> UiText.StringResource(R.string.error_no_internet)
    DataError.Network.Timeout                       -> UiText.StringResource(R.string.error_timeout)
    DataError.Network.Unauthorized                  -> UiText.StringResource(R.string.error_unauthorized)
    DataError.Network.Serialization,
    DataError.Network.ServerError,
    DataError.Network.Unknown                       -> UiText.StringResource(R.string.error_unknown)
    is DataError.Network.Http                       -> when (code) {
        401  -> UiText.StringResource(R.string.error_unauthorized)
        404  -> UiText.StringResource(R.string.error_account_not_found)
        else -> UiText.StringResource(R.string.error_unknown)
    }
}
