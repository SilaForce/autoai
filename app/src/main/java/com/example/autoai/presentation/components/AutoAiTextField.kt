package com.example.autoai.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.theme.CharcoalGray
import com.example.autoai.presentation.theme.PureWhite

@Composable
fun AutoAiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = CharcoalGray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Wrap in a Box so we can add a transparent overlay for click-interception.
        // Applying Modifier.clickable directly to OutlinedTextField does NOT work because
        // the TextField's own internal pointer-input pipeline (BasicTextField) consumes
        // taps in the Main pass before the outer Modifier.clickable can fire.
        // The overlay Box is rendered last, sits on top in z-order, and wins the hit-test.
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = Color.Gray.copy(alpha = 0.7f),
                        fontSize = 15.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = keyboardOptions,
                readOnly = readOnly,

                visualTransformation = if (isPassword && !isPasswordVisible) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },

                trailingIcon = if (isPassword) {
                    {
                        val image = if (isPasswordVisible) {
                            Icons.Outlined.Visibility
                        } else {
                            Icons.Outlined.VisibilityOff
                        }

                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = image,
                                contentDescription = if (isPasswordVisible) {
                                    AppStrings.Common.hidePassword
                                } else {
                                    AppStrings.Common.showPassword
                                },
                                tint = Color.Gray
                            )
                        }
                    }
                } else null,

                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite,
                    focusedBorderColor = CharcoalGray,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                    cursorColor = CharcoalGray
                )
            )

            // Transparent overlay — only added when an onClick action is needed.
            // Being rendered last, it sits on top in z-order and captures the tap
            // before the TextField's internal gesture handler can consume it.
            if (onClick != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(onClick = onClick)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AutoAiTextFieldPreview() {
    Column(Modifier.padding(16.dp)) {
        AutoAiTextField(
            value = "",
            onValueChange = {},
            label = "Email",
            placeholder = "your@email.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        AutoAiTextField(
            value = "myPassword123",
            onValueChange = {},
            label = "Password",
            placeholder = "••••••••",
            isPassword = true
        )
    }
}