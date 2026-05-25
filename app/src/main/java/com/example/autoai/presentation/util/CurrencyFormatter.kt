package com.example.autoai.presentation.util

object CurrencyFormatter {

    val supportedCurrencies: List<String> = listOf("BAM", "EUR", "USD")

    fun symbolFor(currencyCode: String): String = when (currencyCode.uppercase()) {
        "BAM" -> "KM"
        "EUR" -> "€"
        "USD" -> "$"
        else -> currencyCode
    }

    fun format(amount: String, currencyCode: String): String =
        "$amount ${symbolFor(currencyCode)}"
}
