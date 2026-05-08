package com.example.autoai.presentation.features.costs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.TireRepair
import com.example.domain.model.cost.Cost
import com.example.domain.model.cost.CostCategory
import com.example.domain.model.cost.CostStatistics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun dateFormatter() = SimpleDateFormat("MMM d", Locale.getDefault())

fun Cost.toCostItemUi(): CostItemUi {
    val titleParts = listOfNotNull(
        description?.takeIf { it.isNotBlank() },
        location?.takeIf { it.isNotBlank() },
    )
    val title = when {
        titleParts.isNotEmpty() -> titleParts.joinToString(" - ")
        else -> category.toDisplayName()
    }

    return CostItemUi(
        id = id,
        title = title,
        subtitle = dateFormatter().format(Date(dateMillis)),
        amount = "${amount.toFormattedAmount()} KM",
        categoryIcon = category.toIcon(),
    )
}

fun CostStatistics.toCostStatsUi(): CostStatsUi {
    val maxAmount = amountByCategory.values.maxOrNull()?.takeIf { it > 0 } ?: 1.0

    val breakdowns = amountByCategory
        .entries
        .sortedByDescending { it.value }
        .map { (category, amount) ->
            CostStatsByCategoryUi(
                categoryName = category.toDisplayName(),
                amount = "${amount.toFormattedAmount()} KM",
                progress = (amount / maxAmount).toFloat().coerceIn(0f, 1f),
            )
        }

    return CostStatsUi(
        totalAmount = "${totalAmount.toFormattedAmount()} KM",
        categoryBreakdowns = breakdowns,
    )
}

private fun CostCategory.toDisplayName(): String = when (this) {
    CostCategory.FUEL -> "Gorivo"
    CostCategory.SERVICE -> "Servis"
    CostCategory.TIRES -> "Gume"
    CostCategory.EQUIPMENT -> "Oprema"
    CostCategory.OTHER -> "Ostalo"
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




