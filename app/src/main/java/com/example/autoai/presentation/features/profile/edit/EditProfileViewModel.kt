package com.example.autoai.presentation.features.profile.edit

import androidx.lifecycle.viewModelScope
import com.example.autoai.R
import com.example.autoai.base.BaseViewModel
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import com.example.autoai.presentation.util.ImageUtils
import com.example.autoai.presentation.util.UiText
import com.example.autoai.presentation.util.asUiText
import com.example.domain.model.app.onFailure
import com.example.domain.model.app.onSuccess
import com.example.domain.usecase.user.DeleteUserUseCase
import com.example.domain.usecase.user.GetCurrentUserUseCase
import com.example.domain.usecase.user.UpdateUserParams
import com.example.domain.usecase.user.UpdateUserUseCase
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val navigator: IAppNavigator
) : BaseViewModel<EditProfileState, EditProfileEvent, EditProfileSideEffect>(EditProfileState()) {

    init {
        loadCurrentUser()
    }

    override fun onEvent(event: EditProfileEvent) {
        when (event) {
            is EditProfileEvent.OnUsernameChange ->
                setState { it.copy(username = event.value) }

            is EditProfileEvent.OnFullNameChange ->
                setState { it.copy(fullName = event.value, fullNameError = null) }

            is EditProfileEvent.OnPhoneChange ->
                setState { it.copy(phoneNumber = event.value) }

            EditProfileEvent.OnSaveClick -> saveProfile()

            EditProfileEvent.OnDeleteAccountClick ->
                setState { it.copy(showDeleteConfirmDialog = true) }

            EditProfileEvent.OnDeleteConfirmed -> deleteAccount()

            EditProfileEvent.OnDeleteDismissed ->
                setState { it.copy(showDeleteConfirmDialog = false) }

            EditProfileEvent.OnBackClicked -> navigateBack()

            EditProfileEvent.OnChangeAvatarClick ->
                emitSideEffect(
                    EditProfileSideEffect.ShowMessage(
                        UiText.StringResource(R.string.edit_profile_avatar_coming_soon)
                    )
                )

            is EditProfileEvent.OnImageSelected ->
                setState { it.copy(selectedProfilePicture = event.imageBytes) }

        }
    }

    private fun navigateBack() {
     navigator.navigateBack()
    }

    private fun loadCurrentUser() {
        setState { it.copy(isLoading = true) }
        viewModelScope.launch {
            getCurrentUserUseCase(Unit)
                .onSuccess { user ->
                    setState {
                        it.copy(
                            isLoading = false,
                            userId = user.id,
                            username = user.username,
                            fullName = user.name,
                            email = user.email,
                            phoneNumber = user.phoneNumber,
                            userInitial = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "",
                            profilePictureUrl = user.profilePictureUrl
                        )
                    }
                }
                .onFailure { error ->
                    setState { it.copy(isLoading = false) }
                    emitSideEffect(EditProfileSideEffect.ShowMessage(error.asUiText()))
                }
        }
    }

    private fun saveProfile() {
        val current = state.value

        if (current.fullName.isBlank()) {
            setState {
                it.copy(fullNameError = UiText.StringResource(R.string.edit_profile_error_name_empty))
            }
            return
        }

        setState { it.copy(isSaving = true, fullNameError = null) }

        viewModelScope.launch {

            if (current.selectedProfilePicture != null) {
                // 1. Kompresujemo sliku i pretvaramo je u String pomoću našeg novog alata
                val base64Image = ImageUtils.compressAndEncodeToBase64(current.selectedProfilePicture)

                // 2. Šaljemo taj tekst pravo u postojeći UpdateUserUseCase
                updateUserUseCase(
                    UpdateUserParams(
                        name = current.fullName,
                        username = current.username,
                        phoneNumber = current.phoneNumber,
                        profilePictureUrl = base64Image // Ovdje sada ide naš Base64 string!
                    )
                ).onSuccess {
                    setState { it.copy(isSaving = false) }
                    emitSideEffect(EditProfileSideEffect.ShowMessage(UiText.StringResource(R.string.edit_profile_save_success)))
                    navigator.navigateBack()
                }.onFailure { error ->
                    setState { it.copy(isSaving = false) }
                    emitSideEffect(EditProfileSideEffect.ShowMessage(error.asUiText()))
                }
            } else {
                updateUserUseCase(
                    UpdateUserParams(
                        name = current.fullName,
                        username = current.username,
                        phoneNumber = current.phoneNumber,
                        profilePictureUrl = current.profilePictureUrl
                    )
                )
                    .onSuccess {
                        setState { it.copy(isSaving = false) }
                        emitSideEffect(EditProfileSideEffect.ShowMessage(UiText.StringResource(R.string.edit_profile_save_success)))
                        navigator.navigateBack()
                    }
                    .onFailure { error ->
                        setState { it.copy(isSaving = false) }
                        emitSideEffect(EditProfileSideEffect.ShowMessage(error.asUiText()))
                    }
            }
        }
    }

    private fun deleteAccount() {
        setState { it.copy(isDeleting = true, showDeleteConfirmDialog = false) }
        viewModelScope.launch {
            deleteUserUseCase(Unit)
                .onSuccess {
                    setState { it.copy(isDeleting = false) }
                    navigator.navigateTo(
                        destination = Route.AuthGraph,
                        popUpTo = 0,
                        inclusive = true
                    )
                }
                .onFailure { error ->
                    setState { it.copy(isDeleting = false) }
                    emitSideEffect(EditProfileSideEffect.ShowMessage(error.asUiText()))
                }
        }
    }
}

