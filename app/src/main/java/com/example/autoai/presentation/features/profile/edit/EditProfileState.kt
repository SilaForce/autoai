package com.example.autoai.presentation.features.profile.edit

import com.example.autoai.presentation.util.UiText

data class EditProfileState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val username: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val userInitial: String = "",
    val showDeleteConfirmDialog: Boolean = false,
    val fullNameError: UiText? = null,
)

