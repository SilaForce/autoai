package com.example.domain.util

import java.time.Year

/**
 * Centralizovani alat za validaciju svih korisničkih unosa.
 * Sva pravila su u Domain sloju i lako se mogu testirati.
 */
object ValidationUtil {
    // Industrijski standard za validaciju emaila
    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9!#\$%&'*+/=?^_`{|}~-]+" +
                "(?:\\.[A-Za-z0-9!#\$%&'*+/=?^_`{|}~-]+)*" +
                "@" +
                "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)*" +
                "[A-Za-z]{2,63}$"
    )

    // Name: Unicode letters (\p{L} covers Latin-1, diacritics, Cyrillic, etc.), spaces, hyphens. 2-100 chars.
    private val FULL_NAME_REGEX = Regex("^[\\p{L}][\\p{L} '\\-]{0,98}[\\p{L}]$")

    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        if (email.length > 254) return false
        return EMAIL_REGEX.matches(email.trim())
    }

    fun isValidFullName(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.length < 2 || trimmed.length > 100) return false
        return FULL_NAME_REGEX.matches(trimmed)
    }

    /**
     * Firebase Auth zahtijeva minimalno 6 karaktera za lozinku.
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidVehicleText(value: String): Boolean {
        return value.trim().isNotBlank()
    }

    fun isValidVehicleYear(year: Int): Boolean {
        val currentYear = Year.now().value
        return year in 1886..(currentYear + 1)
    }

    fun isValidMileage(mileage: Int?): Boolean {
        return mileage == null || mileage >= 0
    }
}