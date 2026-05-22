package com.example.autoai.presentation.features.costs.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.features.costs.toIcon
import androidx.compose.material3.MaterialTheme
import com.example.autoai.presentation.theme.VerdantGreen
import com.example.domain.model.cost.CostCategory

@Composable
fun CategorySelectionRow(
    selectedCategory: CostCategory,
    onCategorySelected: (CostCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = listOf(
        CostCategory.FUEL to AppStrings.Costs.categoryFuel,
        CostCategory.SERVICE to AppStrings.Costs.categoryService,
        CostCategory.TIRES to AppStrings.Costs.categoryTires,
        CostCategory.EQUIPMENT to AppStrings.Costs.categoryEquipment,
        CostCategory.OTHER to AppStrings.Costs.categoryOther,
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.forEach { (category, label) ->
            val isSelected = category == selectedCategory
            Surface(
                onClick = { onCategorySelected(category) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) VerdantGreen else MaterialTheme.colorScheme.outlineVariant,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = category.toIcon(),
                        contentDescription = label,
                        tint = if (isSelected) VerdantGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) VerdantGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

