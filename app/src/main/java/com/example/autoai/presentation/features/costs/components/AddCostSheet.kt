package com.example.autoai.presentation.features.costs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.components.AutoAiTextField
import com.example.autoai.presentation.components.MainButton
import com.example.autoai.presentation.features.costs.CostsEvent
import com.example.autoai.presentation.features.costs.CostsState

@Composable
fun AddCostSheet(
    state: CostsState,
    onEvent: (CostsEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .imePadding(),
    ) {
        Text(
            text = if (state.editingCostId != null) AppStrings.Costs.editSheetTitle else AppStrings.Costs.addSheetTitle,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = AppStrings.Costs.categoryLabel,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(10.dp))

        CategorySelectionRow(
            selectedCategory = state.selectedCategory,
            onCategorySelected = { onEvent(CostsEvent.OnCategorySelected(it)) },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AutoAiTextField(
                modifier = Modifier.weight(1f),
                value = state.amountInput,
                onValueChange = { onEvent(CostsEvent.OnAmountChanged(it)) },
                label = AppStrings.Costs.amountLabel,
                placeholder = AppStrings.Costs.amountPlaceholder,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            AutoAiTextField(
                modifier = Modifier.weight(1f),
                value = state.locationInput,
                onValueChange = { onEvent(CostsEvent.OnLocationChanged(it)) },
                label = AppStrings.Costs.locationLabel,
                placeholder = AppStrings.Costs.locationPlaceholder,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        AutoAiTextField(
            value = state.descriptionInput,
            onValueChange = { onEvent(CostsEvent.OnDescriptionChanged(it)) },
            label = AppStrings.Costs.descriptionLabel,
            placeholder = AppStrings.Costs.descriptionPlaceholder,
        )

        Spacer(modifier = Modifier.height(24.dp))

        MainButton(
            text = if (state.editingCostId != null) AppStrings.Costs.updateButton else AppStrings.Costs.saveButton,
            onClick = { onEvent(CostsEvent.OnSaveCostClicked) },
            enabled = !state.isSaving,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
