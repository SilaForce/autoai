package com.example.autoai.presentation.features.profile.edit

import androidx.lifecycle.viewModelScope
import com.example.autoai.R
import com.example.autoai.base.BaseViewModel
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

            EditProfileEvent.OnChangeAvatarClick ->
                emitSideEffect(
                    EditProfileSideEffect.ShowMessage(
                        UiText.StringResource(R.string.edit_profile_avatar_coming_soon)
                    )
                )
        }
    }

    // ─── Private ─────────────────────────────────────────────────────────────

    private fun loadCurrentUser() {
        setState { it.copy(isLoading = true) }
        viewModelScope.launch {
            getCurrentUserUseCase(Unit)
                .onSuccess { user ->
                    setState {
                        it.copy(
                            isLoading = false,
                            username = user.username,
                            fullName = user.name,
                            email = user.email,
                            phoneNumber = user.phoneNumber,
                            userInitial = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "",
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
            updateUserUseCase(
                UpdateUserParams(
                    name = current.fullName,
                    username = current.username,
                    phoneNumber = current.phoneNumber,
                )
            )
                .onSuccess {
                    setState { it.copy(isSaving = false) }
                    emitSideEffect(
                        EditProfileSideEffect.ShowMessage(
                            UiText.StringResource(R.string.edit_profile_save_success)
                        )
                    )
                    emitSideEffect(EditProfileSideEffect.NavigateBack)
                }
                .onFailure { error ->
                    setState { it.copy(isSaving = false) }
                    emitSideEffect(EditProfileSideEffect.ShowMessage(error.asUiText()))
                }
        }
    }

    private fun deleteAccount() {
        setState { it.copy(isDeleting = true, showDeleteConfirmDialog = false) }
        viewModelScope.launch {
            deleteUserUseCase(Unit)
                .onSuccess {
                    setState { it.copy(isDeleting = false) }
                    emitSideEffect(EditProfileSideEffect.NavigateToAuth)
                }
                .onFailure { error ->
                    setState { it.copy(isDeleting = false) }
                    emitSideEffect(EditProfileSideEffect.ShowMessage(error.asUiText()))
                }
        }
    }
}

