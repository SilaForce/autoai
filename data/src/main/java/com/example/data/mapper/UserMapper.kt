package com.example.data.mapper

import com.example.data.model.user.UserDto
import com.example.domain.model.user.User

/**
 * `email` comes from FirebaseAuth; pass it in alongside the Firestore-sourced DTO so the
 * domain model stays internally consistent.
 */
fun UserDto.toUser(email: String): User {
    return User(
        id = id,
        email = email,
        name = name,
        username = username,
        phoneNumber = phoneNumber,
        profilePictureUrl = profilePictureUrl,
        isPremium = isPremium,
        currency = currency,
        createdAt = createdAt,
    )
}

fun User.toUserDto(): UserDto {
    return UserDto(
        id = id,
        name = name,
        username = username,
        phoneNumber = phoneNumber,
        profilePictureUrl = profilePictureUrl,
        isPremium = isPremium,
        currency = currency,
        createdAt = createdAt,
    )
}

/**
 * Builds the partial-update map for [com.example.data.repository.auth.FirebaseAuthRepository.updateUser].
 * Server-managed fields ([User.isPremium], [User.createdAt]) are deliberately omitted so a
 * client-side update doesn't overwrite them. `profilePictureUrl` is included only when
 * non-null — passing null would explicitly nuke the field, which isn't what the UI intends
 * when the picture wasn't changed.
 */
fun User.toUserUpdateMap(): Map<String, Any> {
    val updates = mutableMapOf<String, Any>(
        "name" to name,
        "username" to username,
        "phoneNumber" to phoneNumber,
        "currency" to currency,
    )
    profilePictureUrl?.let { updates["profilePictureUrl"] = it }
    return updates
}
