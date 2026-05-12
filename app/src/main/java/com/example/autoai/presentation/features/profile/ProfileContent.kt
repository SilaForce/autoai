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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
                    userName = state.userName,
                    userEmail = state.userEmail,
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

// ─── Private composables ──────────────────────────────────────────────────────

@Composable
private fun UserHeaderCard(
    userInitial: String,
    userName: String,
    userEmail: String,
    onEditClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .background(color = VerdantGreen, shape = CircleShape),
            ) {
                Text(
                    text = userInitial,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = userName,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = CharcoalGray,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = userEmail,
                fontSize = 14.sp,
                color = CharcoalGray.copy(alpha = 0.6f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onEditClick,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = AppStrings.Profile.editButton,
                    color = CharcoalGray,
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = count.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = VerdantGreen,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                fontSize = 12.sp,
                color = CharcoalGray.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AccountInfoCard(
    memberSince: String,
    plan: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = AppStrings.Profile.accountInfoTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = CharcoalGray,
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = AppStrings.Profile.memberSinceLabel, value = memberSince)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = SubtleBorder,
            )

            InfoRow(label = AppStrings.Profile.planLabel, value = plan)
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = CharcoalGray.copy(alpha = 0.6f),
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = CharcoalGray,
        )
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

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
