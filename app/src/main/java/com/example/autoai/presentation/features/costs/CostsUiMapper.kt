package com.example.autoai.presentation.features.costs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.TireRepair
import com.example.autoai.R
import com.example.autoai.presentation.util.UiText
import com.example.domain.model.cost.Cost
import com.example.domain.model.cost.CostCategory
import com.example.domain.model.cost.CostStatistics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun dateFormatter() = SimpleDateFormat("MMM d", Locale.getDefault())

fun Cost.toCostItemUi(): CostItemUi {
    val trimmedDescription = description?.takeIf { it.isNotBlank() }
    val title: UiText = if (trimmedDescription != null) {
        UiText.DynamicString(trimmedDescription)
    } else {
        UiText.StringResource(category.toCategoryStringRes())
    }

    val date = dateFormatter().format(Date(dateMillis))
    val trimmedLocation = location?.takeIf { it.isNotBlank() }
    val subtitle = if (trimmedLocation != null) "$date · $trimmedLocation" else date

    return CostItemUi(
        id = id,
        title = title,
        subtitle = subtitle,
        amount = "${amount.toFormattedAmount()} KM",
        categoryIcon = category.toIcon(),
    )
}

fun CostStatistics.toCostStatsUi(): CostStatsUi {
    val total = totalAmount

    val breakdowns = amountByCategory.entries
        .sortedByDescending { it.value }
        .map { (category, amount) ->
            val count = countByCategory[category] ?: 0
            val avg = if (count > 0) amount / count else 0.0
            CostStatsByCategoryUi(
                category = category,
                categoryName = UiText.StringResource(category.toCategoryStringRes()),
                amount = "${amount.toFormattedAmount()} KM",
                percentage = if (total > 0) ((amount / total) * 100).toInt() else 0,
                count = count,
                averagePerEntry = "${avg.toFormattedAmount()} KM",
                progress = if (total > 0) (amount / total).toFloat().coerceIn(0f, 1f) else 0f,
            )
        }

    return CostStatsUi(
        totalAmount = "${totalAmount.toFormattedAmount()} KM",
        categoryBreakdowns = breakdowns,
    )
}

fun CostCategory.toCategoryStringRes(): Int = when (this) {
    CostCategory.FUEL -> R.string.costs_category_fuel
    CostCategory.SERVICE -> R.string.costs_category_service
    CostCategory.TIRES -> R.string.costs_category_tires
    CostCategory.EQUIPMENT -> R.string.costs_category_equipment
    CostCategory.OTHER -> R.string.costs_category_other
}

fun CostCategory.toIcon() = when (this) {
    CostCategory.FUEL -> Icons.Outlined.LocalGasStation
    CostCategory.SERVICE -> Icons.Outlined.Build
    CostCategory.TIRES -> Icons.Outlined.TireRepair
    CostCategory.EQUIPMENT -> Icons.Outlined.Inventory2
    CostCategory.OTHER -> Icons.Outlined.MoreHoriz
}

private fun Double.toFormattedAmount(): String {
    return if (this == kotlin.math.floor(this)) {
        this.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.2f", this)
    }
}
