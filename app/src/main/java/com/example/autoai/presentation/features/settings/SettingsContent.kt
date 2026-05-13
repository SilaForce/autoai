package com.example.autoai.presentation.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.features.settings.components.SettingsItemCard
import com.example.autoai.presentation.theme.CharcoalGray
import com.example.autoai.presentation.theme.OffWhiteBg
import com.example.autoai.presentation.theme.PureWhite
import com.example.autoai.presentation.theme.VerdantGreen
import com.example.autoai.presentation.theme.WarningOrange
import com.example.autoai.presentation.theme.WarningOrangeBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    state: SettingsState,
    snackbarHostState: SnackbarHostState,
    onEvent: (SettingsEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.Settings.titleStr,
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalGray
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SettingsEvent.OnBackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Nazad",
                            tint = CharcoalGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OffWhiteBg)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = OffWhiteBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {

            SettingsItemCard(
                icon = Icons.Outlined.Notifications,
                title = AppStrings.Settings.notificationsStr,
                trailingContent = {
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = { onEvent(SettingsEvent.OnToggleNotifications(it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureWhite,
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
                            checkedThumbColor = PureWhite,
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
                        tint = CharcoalGray.copy(alpha = 0.5f)
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
                        tint = CharcoalGray.copy(alpha = 0.5f)
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
        }
    }
}