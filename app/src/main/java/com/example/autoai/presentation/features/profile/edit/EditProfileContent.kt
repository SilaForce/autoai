package com.example.autoai.presentation.features.profile.edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.features.profile.edit.components.AvatarSection
import com.example.autoai.presentation.features.profile.edit.components.DangerZoneCard
import com.example.autoai.presentation.features.profile.edit.components.SectionCard
import com.example.autoai.presentation.theme.AutoAITheme
import com.example.autoai.presentation.theme.DangerRed
import com.example.autoai.presentation.theme.VerdantGreen

@Composable
fun EditProfileContent(
    state: EditProfileState,
    snackbarHostState: SnackbarHostState,
    onEvent: (EditProfileEvent) -> Unit,
) {
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    onEvent(EditProfileEvent.OnImageSelected(bytes))
                }
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = VerdantGreen,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp),
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                // ── Header ────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(onClick = { onEvent(EditProfileEvent.OnBackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = AppStrings.Common.back,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Text(
                        text = AppStrings.EditProfile.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Avatar section ────────────────────────────────────
                AvatarSection(
                    userInitial = state.userInitial,
                    profilePictureUrl = state.profilePictureUrl,
                    selectedProfilePicture = state.selectedProfilePicture,
                    onAvatarClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Profile info card ─────────────────────────────────
                SectionCard(title = AppStrings.EditProfile.sectionProfile) {
                    OutlinedTextField(
                        value = state.username,
                        onValueChange = { onEvent(EditProfileEvent.OnUsernameChange(it)) },
                        label = { Text(AppStrings.EditProfile.usernameLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.fullName,
                        onValueChange = { onEvent(EditProfileEvent.OnFullNameChange(it)) },
                        label = { Text(AppStrings.EditProfile.fullNameLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = state.fullNameError != null,
                        supportingText = state.fullNameError?.let {
                            { Text(it.asString(), color = DangerRed) }
                        },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Contact card ──────────────────────────────────────
                SectionCard(title = AppStrings.EditProfile.sectionContact) {
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = {},
                        label = { Text(AppStrings.EditProfile.emailLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        ),
                        supportingText = {
                            Text(
                                text = AppStrings.EditProfile.emailReadonlyNote,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                            )
                        },
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.phoneNumber,
                        onValueChange = { onEvent(EditProfileEvent.OnPhoneChange(it)) },
                        label = { Text(AppStrings.EditProfile.phoneLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Save button ───────────────────────────────────────
                Button(
                    onClick = { onEvent(EditProfileEvent.OnSaveClick) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerdantGreen),
                    enabled = !state.isSaving && !state.isDeleting,
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = AppStrings.EditProfile.saveButton,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Danger zone card ──────────────────────────────────
                DangerZoneCard(
                    isDeleting = state.isDeleting,
                    onDeleteClick = { onEvent(EditProfileEvent.OnDeleteAccountClick) },
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // ── Delete confirmation dialog ──────────────────────────────
        if (state.showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { onEvent(EditProfileEvent.OnDeleteDismissed) },
                title = {
                    Text(
                        text = AppStrings.EditProfile.deleteConfirmTitle,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                text = {
                    Text(
                        text = AppStrings.EditProfile.deleteConfirmMessage,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                },
                confirmButton = {
                    TextButton(onClick = { onEvent(EditProfileEvent.OnDeleteConfirmed) }) {
                        Text(
                            text = AppStrings.EditProfile.deleteConfirmButton,
                            color = DangerRed,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(EditProfileEvent.OnDeleteDismissed) }) {
                        Text(
                            text = AppStrings.EditProfile.cancel,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}


// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun EditProfileContentPreview() {
    AutoAITheme {
        EditProfileContent(
            state = EditProfileState(
                isLoading = false,
                username = "amarsilajdzic",
                fullName = "Amar Silajdžić",
                email = "amar@example.com",
                phoneNumber = "+387 61 123 456",
                userInitial = "A",
                profilePictureUrl = null,
                selectedProfilePicture = null,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditProfileContentLoadingPreview() {
    AutoAITheme {
        EditProfileContent(
            state = EditProfileState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
