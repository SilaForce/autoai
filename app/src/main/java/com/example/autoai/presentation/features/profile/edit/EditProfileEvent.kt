package com.example.autoai.presentation.features.profile.edit

import com.example.autoai.presentation.util.UiText

sealed interface EditProfileEvent {
    data class OnUsernameChange(val value: String) : EditProfileEvent
    data class OnFullNameChange(val value: String) : EditProfileEvent
    data class OnPhoneChange(val value: String) : EditProfileEvent
    data object OnSaveClick : EditProfileEvent
    data object OnDeleteAccountClick : EditProfileEvent
    data object OnDeleteConfirmed : EditProfileEvent
    data object OnDeleteDismissed : EditProfileEvent
    data object OnBackClicked : EditProfileEvent
    data object OnChangeAvatarClick : EditProfileEvent
    data class OnImageSelected(val imageBytes: ByteArray) : EditProfileEvent
}

sealed interface EditProfileSideEffect {
    data object NavigateBack : EditProfileSideEffect
    data object NavigateToAuth : EditProfileSideEffect
    data class ShowMessage(val message: UiText) : EditProfileSideEffect
}

