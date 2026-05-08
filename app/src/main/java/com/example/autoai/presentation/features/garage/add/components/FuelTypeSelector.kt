package com.example.autoai.presentation.features.garage.add.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.autoai.presentation.features.garage.add.FuelTypeOptionUi
import com.example.autoai.presentation.theme.CharcoalGray
import com.example.autoai.presentation.theme.PureWhite
import com.example.autoai.presentation.theme.VerdantGreen
import com.example.domain.model.vehicle.FuelType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FuelTypeSelector(
    options: List<FuelTypeOptionUi>,
    onFuelTypeSelected: (FuelType) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option.isSelected,
                onClick = { onFuelTypeSelected(option.fuelType) },
                label = {
                    Text(
                        text = option.label.asString(),
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VerdantGreen,
                    selectedLabelColor = PureWhite,
                    containerColor = PureWhite,
                    labelColor = CharcoalGray,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = option.isSelected,
                    borderColor = if (option.isSelected) VerdantGreen else CharcoalGray.copy(alpha = 0.18f),
                    selectedBorderColor = VerdantGreen,
                ),
            )
        }
    }
}
