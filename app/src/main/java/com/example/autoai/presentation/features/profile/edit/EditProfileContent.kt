package com.example.autoai.presentation.features.profile.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.theme.AutoAITheme
import com.example.autoai.presentation.theme.CharcoalGray
import com.example.autoai.presentation.theme.OffWhiteBg
import com.example.autoai.presentation.theme.PureWhite
import com.example.autoai.presentation.theme.SubtleBorder
import com.example.autoai.presentation.theme.VerdantGreen

private val DangerRed = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(
    state: EditProfileState,
    snackbarHostState: SnackbarHostState,
    onEvent: (EditProfileEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.EditProfile.title,
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalGray,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(EditProfileEvent.OnDeleteDismissed) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = AppStrings.Common.back,
                            tint = CharcoalGray,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OffWhiteBg),
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(top = 8.dp),
            )
        },
        containerColor = OffWhiteBg,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Avatar section ─────────────────────────────────────
                    AvatarSection(
                        userInitial = state.userInitial,
                        onAvatarClick = { onEvent(EditProfileEvent.OnChangeAvatarClick) },
                    )

                    // ── Profile info card ──────────────────────────────────
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

                    // ── Contact card ───────────────────────────────────────
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
                                disabledTextColor = CharcoalGray.copy(alpha = 0.5f),
                                disabledBorderColor = SubtleBorder,
                                disabledLabelColor = CharcoalGray.copy(alpha = 0.4f),
                            ),
                            supportingText = {
                                Text(
                                    text = AppStrings.EditProfile.emailReadonlyNote,
                                    fontSize = 11.sp,
                                    color = CharcoalGray.copy(alpha = 0.45f),
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

                    // ── Save button ────────────────────────────────────────
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
                                color = PureWhite,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = AppStrings.EditProfile.saveButton,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PureWhite,
                            )
                        }
                    }

                    // ── Danger zone card ───────────────────────────────────
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
                            color = CharcoalGray,
                        )
                    },
                    text = {
                        Text(
                            text = AppStrings.EditProfile.deleteConfirmMessage,
                            color = CharcoalGray.copy(alpha = 0.7f),
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
                                color = CharcoalGray,
                            )
                        }
                    },
                    containerColor = PureWhite,
                )
            }
        }
    }
}

// ─── Private composables ──────────────────────────────────────────────────────

@Composable
private fun AvatarSection(
    userInitial: String,
    onAvatarClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clickable(onClick = onAvatarClick),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(88.dp)
                    .background(color = VerdantGreen, shape = CircleShape),
            ) {
                Text(
                    text = userInitial,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite,
                )
            }

            // Camera badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.BottomEnd)
                    .background(color = VerdantGreen, shape = CircleShape)
                    .padding(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = AppStrings.EditProfile.changeAvatar,
            fontSize = 13.sp,
            color = VerdantGreen,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, SubtleBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = CharcoalGray.copy(alpha = 0.65f),
                modifier = Modifier.padding(bottom = 12.dp),
            )
            content()
        }
    }
}

@Composable
private fun DangerZoneCard(
    isDeleting: Boolean,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = AppStrings.EditProfile.dangerZone,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DangerRed.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 12.dp),
            )

            TextButton(
                onClick = onDeleteClick,
                enabled = !isDeleting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = DangerRed,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 4.dp),
                    )
                    Text(
                        text = AppStrings.EditProfile.deleteAccount,
                        color = DangerRed,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

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

