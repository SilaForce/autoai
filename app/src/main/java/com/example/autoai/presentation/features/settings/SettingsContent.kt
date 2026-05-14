package com.example.autoai.presentation.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.features.settings.components.SettingsItemCard
import com.example.autoai.presentation.theme.VerdantGreen
import com.example.autoai.presentation.theme.WarningOrange
import com.example.autoai.presentation.theme.WarningOrangeBg

@Composable
fun SettingsContent(
    state: SettingsState,
    snackbarHostState: SnackbarHostState,
    onEvent: (SettingsEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // ── Header ────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(onClick = { onEvent(SettingsEvent.OnBackClicked) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Text(
                    text = AppStrings.Settings.titleStr,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsItemCard(
                icon = Icons.Outlined.Notifications,
                title = AppStrings.Settings.notificationsStr,
                trailingContent = {
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = { onEvent(SettingsEvent.OnToggleNotifications(it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.surface,
                            checkedTrackColor = VerdantGreen
                        )
                    )
                }
            )

            SettingsItemCard(
                icon = Icons.Outlined.DarkMode,
                title = AppStrings.Settings.darkModeStr,
                trailingContent = {
                    Switch(
                        checked = state.isDarkModeEnabled,
                        onCheckedChange = { onEvent(SettingsEvent.OnToggleDarkMode(it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.surface,
                            checkedTrackColor = VerdantGreen
                        )
                    )
                }
            )

            SettingsItemCard(
                icon = Icons.Outlined.Language,
                title = AppStrings.Settings.languageStr,
                onClick = { onEvent(SettingsEvent.OnChangeLanguageClicked) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            )

            SettingsItemCard(
                icon = Icons.Outlined.Security,
                title = AppStrings.Settings.privacyPolicyStr,
                onClick = { onEvent(SettingsEvent.OnPrivacyPolicyClicked) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsItemCard(
                icon = Icons.AutoMirrored.Outlined.ExitToApp,
                title = AppStrings.Settings.logoutStr,
                onClick = { onEvent(SettingsEvent.OnLogOutClicked) },
                iconBackgroundColor = WarningOrangeBg,
                iconColor = WarningOrange,
                titleColor = WarningOrange,
                trailingContent = {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = WarningOrange,
                            strokeWidth = 2.dp
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}
