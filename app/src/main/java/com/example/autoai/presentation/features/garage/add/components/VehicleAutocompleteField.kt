package com.example.autoai.presentation.features.garage.add.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    isLoading: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = {
            if (enabled) onExpandedChange(it)
        },
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = true,
            enabled = enabled,
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                }
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = enabled)
                .fillMaxWidth(),
        )

        ExposedDropdownMenu(
            expanded = isExpanded && options.isNotEmpty(),
            onDismissRequest = { onExpandedChange(false) },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onOptionSelected(option) },
                )
            }
        }
    }
}
