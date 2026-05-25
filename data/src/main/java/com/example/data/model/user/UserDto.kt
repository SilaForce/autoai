package com.example.data.model.user

import com.google.firebase.firestore.PropertyName

/**
 * Firestore representation of [com.example.domain.model.user.User].
 *
 * Domain models are never written to Firestore directly; the wire format lives here so a
 * future rename or restructure of [com.example.domain.model.user.User] doesn't silently
 * break already-stored documents.
 *
 * `email` is intentionally omitted — it lives in FirebaseAuth, not the Firestore user doc.
 * `id` is sourced from the document id at read time and not persisted as a field.
 */
data class UserDto(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String? = null,
    // Same Java-bean stripping gotcha as VehicleDto.isActive — force literal field name.
    @get:PropertyName("isPremium")
    @set:PropertyName("isPremium")
    var isPremium: Boolean = false,
    val currency: String = "BAM",
    val createdAt: Long = 0L,
)
