package com.example.autoai.presentation.features.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.features.profile.components.AccountInfoCard
import com.example.autoai.presentation.features.profile.components.StatCard
import com.example.autoai.presentation.features.profile.components.UserHeaderCard
import com.example.autoai.presentation.theme.AutoAITheme
import com.example.autoai.presentation.theme.CharcoalGray
import com.example.autoai.presentation.theme.OffWhiteBg

@Composable
fun ProfileContent(
    state: ProfileState,
    snackbarHostState: SnackbarHostState,
    onEvent: (ProfileEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OffWhiteBg)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                // ── Screen title ───────────────────────────────────────────
                Text(
                    text = AppStrings.Profile.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalGray,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── User header card ───────────────────────────────────────
                UserHeaderCard(
                    userInitial = state.userInitial,
                    fullName = state.userName, // Preimenovali smo parametar zbog jasnoće
                    username = state.username,
                    userEmail = state.userEmail,
                    phoneNumber = state.phoneNumber,
                    profilePictureUrl = state.profilePictureUrl,
                    onEditClick = { onEvent(ProfileEvent.OnEditProfileClick) },
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Stats row ──────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        label = AppStrings.Profile.vehiclesLabel,
                        count = state.vehicleCount,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = AppStrings.Profile.enteredCostsLabel,
                        count = state.totalCostCount,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Account info card ──────────────────────────────────────
                AccountInfoCard(
                    memberSince = state.memberSince,
                    plan = state.plan,
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentLoadedPreview() {
    AutoAITheme {
        ProfileContent(
            state = ProfileState(
                isLoading = false,
                userName = "Amar Silajdžić",
                userEmail = "amar@example.com",
                userInitial = "A",
                memberSince = "April 2026",
                plan = "Premium",
                vehicleCount = 2,
                totalCostCount = 14,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentLoadingPreview() {
    AutoAITheme {
        ProfileContent(
            state = ProfileState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
