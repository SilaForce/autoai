package com.example.autoai.presentation.features.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.theme.VerdantGreen
import com.example.autoai.presentation.util.CurrencyFormatter

@Composable
fun CurrencyPickerDialog(
    currentCurrency: String,
    onDismissRequest: () -> Unit,
    onCurrencySelected: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = AppStrings.Settings.currencyDialogTitleStr) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                CurrencyFormatter.supportedCurrencies.forEach { code ->
                    val isSelected = code == currentCurrency
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCurrencySelected(code) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onCurrencySelected(code) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = VerdantGreen,
                            ),
                        )
                        Text(
                            text = "$code (${CurrencyFormatter.symbolFor(code)})",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = AppStrings.Settings.currencyDialogCancelStr)
            }
        },
    )
}
