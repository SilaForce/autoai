package com.example.autoai.presentation.features.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.components.AutoAiTextField
import com.example.autoai.presentation.components.MainButton
import com.example.autoai.presentation.features.auth.components.AuthTab
import com.example.autoai.presentation.features.auth.components.AuthToggle
import androidx.compose.material3.MaterialTheme

@Composable
fun LoginContent(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // ── Header: green car icon in rounded square ──────────────────────
            Surface(
                modifier = Modifier.size(64.dp),
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.DirectionsCar,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = AppStrings.Auth.garageTitle,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── AuthToggle: Login is the active tab ───────────────────────────
            AuthToggle(
                currentTab = AuthTab.LOGIN,
                onTabChanged = { tab ->
                    if (tab == AuthTab.REGISTER) onEvent(LoginEvent.OnRegisterClicked)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Email field (auto-focused) ────────────────────────────────────
            AutoAiTextField(
                modifier = Modifier.focusRequester(focusRequester),
                value = state.email,
                onValueChange = { onEvent(LoginEvent.OnEmailChange(it)) },
                label = AppStrings.Auth.emailLabel,
                placeholder = AppStrings.Auth.emailPlaceholder,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Password field ────────────────────────────────────────────────
            AutoAiTextField(
                value = state.password,
                onValueChange = { onEvent(LoginEvent.OnPasswordChange(it)) },
                label = AppStrings.Auth.passwordLabel,
                placeholder = AppStrings.Auth.passwordPlaceholder,
                isPassword = true
            )

            // ── Forgot password (right-aligned) ───────────────────────────────
            TextButton(
                onClick = { onEvent(LoginEvent.OnForgotPasswordClicked) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = AppStrings.Auth.forgotPassword,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.End,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Login button ──────────────────────────────────────────────────
            MainButton(
                text = AppStrings.Auth.loginButton,
                onClick = { onEvent(LoginEvent.OnLoginClicked) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    LoginContent(
        state = LoginState(),
        onEvent = {},
        snackbarHostState = remember { SnackbarHostState() }
    )
}

