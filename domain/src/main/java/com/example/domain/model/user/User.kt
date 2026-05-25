package com.example.domain.model.user

data class User(
    val id: String,
    val email: String,
    val name: String,
    val username: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String? = null,
    val isPremium: Boolean = false,
    // ISO 4217 code (BAM/EUR/USD). The display symbol is derived in
    // presentation via CurrencyFormatter.symbolFor(code).
    val currency: String = "BAM",
    val createdAt: Long = System.currentTimeMillis()
)