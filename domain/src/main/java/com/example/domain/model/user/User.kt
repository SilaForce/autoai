package com.example.domain.model.user

data class User(
    val id: String,                  // Jedinstveni ID (koji nam kasnije Firebase dodijeli)
    val email: String,               // Za prijavu i kontakt
    val name: String,                 // Za personalizaciju (npr. "Zdravo, Amare!")
    val profilePictureUrl: String? = null, // Opcionalno, ako korisnik doda sliku
    val isPremium: Boolean = false,  // Jako bitno za AutoAI! (npr. da li ima neograničen pristup AI mehaničaru)
    val currency: String = "BAM",      // Pošto pratiš troškove, dobro je znati koja je valuta korisnika
    val createdAt: Long = System.currentTimeMillis() // Datum kreiranja profila
)