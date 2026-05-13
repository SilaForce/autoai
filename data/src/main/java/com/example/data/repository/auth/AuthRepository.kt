package com.example.data.repository.auth

import com.example.data.datasource.remote.util.safeFirebaseCall
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.app.StartDestination
import com.example.domain.model.user.User
import com.example.domain.repository.IAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
): IAuthRepository {

    override suspend fun checkSession(): StartDestination {
        return if (firebaseAuth.currentUser != null) {
            StartDestination.Home
        } else {
            StartDestination.Auth
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): AppResult<User> {
        return safeFirebaseCall {

            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Firebase user is null")

            val newUser = User(
                id = firebaseUser.uid,
                name = name,
                email = firebaseUser.email ?: email
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(newUser)
                .await()

            newUser
        }
    }

    override suspend fun login(email: String, password: String): AppResult<User> {
        return safeFirebaseCall {

            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Firebase user is null after login")

            val snapshot = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            User(
                id = firebaseUser.uid,
                name = snapshot.getString("name") ?: "",
                username = snapshot.getString("username") ?: "",
                phoneNumber = snapshot.getString("phoneNumber") ?: "",
                email = firebaseUser.email ?: email,
                profilePictureUrl = snapshot.getString("profilePictureUrl"),
                isPremium = snapshot.getBoolean("isPremium") ?: false,
                currency = snapshot.getString("currency") ?: "BAM",
                createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
            )
        }
    }

    override suspend fun getCurrentUser(): AppResult<User> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return AppResult.Failure(DataError.Network.Unauthorized)

        return safeFirebaseCall {
            val snapshot = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            User(
                id = firebaseUser.uid,
                name = snapshot.getString("name") ?: "",
                username = snapshot.getString("username") ?: "",
                phoneNumber = snapshot.getString("phoneNumber") ?: "",
                email = firebaseUser.email ?: "",
                profilePictureUrl = snapshot.getString("profilePictureUrl"),
                isPremium = snapshot.getBoolean("isPremium") ?: false,
                currency = snapshot.getString("currency") ?: "BAM",
                createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
            )
        }
    }

    override suspend fun updateUser(user: User): AppResult<User> {
        return safeFirebaseCall {
            val updates = mutableMapOf<String, Any>(
                "name" to user.name,
                "username" to user.username,
                "phoneNumber" to user.phoneNumber,
            )

            user.profilePictureUrl?.let { url ->
                updates["profilePictureUrl"] = url
             }

            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .update(updates)
                .await()

            user
        }
    }

    override suspend fun deleteUser(): AppResult<Unit> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return AppResult.Failure(DataError.Network.Unauthorized)

        return safeFirebaseCall {
            firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .delete()
                .await()

            firebaseUser.delete().await()
        }
    }

    override suspend fun logout(): AppResult<Unit> {
        return safeFirebaseCall {
            firebaseAuth.signOut()
        }
    }

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}