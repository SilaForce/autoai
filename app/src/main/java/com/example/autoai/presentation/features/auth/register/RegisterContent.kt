package com.example.autoai.presentation.features.auth.register

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
fun RegisterContent(
    state: RegisterState,
    onEvent: (RegisterEvent) -> Unit,
    snackbarHostState: SnackbarHostState // Dodano!
) {
    // 1. Priprema za automatsko fokusiranje
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // 2. ROOT kontejner je sada Box (kako bi Snackbar bio na TopCenter)
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

            AuthToggle(
                currentTab = AuthTab.REGISTER,
                onTabChanged = { tab ->
                    if (tab == AuthTab.LOGIN) onEvent(RegisterEvent.OnLoginClicked)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            AutoAiTextField(
                modifier = Modifier.focusRequester(focusRequester),
                value = state.name,
                onValueChange = { onEvent(RegisterEvent.OnNameChange(it)) },
                label = AppStrings.Auth.nameLabel,
                placeholder = AppStrings.Auth.namePlaceholder
            )

            Spacer(modifier = Modifier.height(16.dp))

            AutoAiTextField(
                value = state.email,
                onValueChange = { onEvent(RegisterEvent.OnEmailChange(it)) },
                label = AppStrings.Auth.emailLabel,
                placeholder = AppStrings.Auth.emailPlaceholder,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AutoAiTextField(
                value = state.password,
                onValueChange = { onEvent(RegisterEvent.OnPasswordChange(it)) },
                label = AppStrings.Auth.passwordLabel,
                placeholder = AppStrings.Auth.passwordPlaceholder,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            AutoAiTextField(
                value = state.confirmPassword,
                onValueChange = { onEvent(RegisterEvent.OnConfirmPasswordChange(it)) },
                label = AppStrings.Auth.confirmPasswordLabel,
                placeholder = AppStrings.Auth.passwordPlaceholder,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(40.dp))

            MainButton(
                text = AppStrings.Auth.createAccountButton,
                onClick = { onEvent(RegisterEvent.OnRegisterClicked) }
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
private fun RegisterPreview() {
    RegisterContent(
        state = RegisterState(),
        onEvent = {},
        snackbarHostState = remember { SnackbarHostState() }
    )
}