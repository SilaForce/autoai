package com.example.autoai.presentation.features.costs

import java.time.LocalDate
import java.time.ZoneId

enum class StatsPeriod { MONTH, LAST_3_MONTHS, YEAR, ALL_TIME }

fun StatsPeriod.sinceMillis(now: LocalDate = LocalDate.now()): Long? {
    val zone = ZoneId.systemDefault()
    return when (this) {
        StatsPeriod.MONTH -> now.withDayOfMonth(1)
            .atStartOfDay(zone).toInstant().toEpochMilli()
        StatsPeriod.LAST_3_MONTHS -> now.minusMonths(2).withDayOfMonth(1)
            .atStartOfDay(zone).toInstant().toEpochMilli()
        StatsPeriod.YEAR -> now.withDayOfYear(1)
            .atStartOfDay(zone).toInstant().toEpochMilli()
        StatsPeriod.ALL_TIME -> null
    }
}
