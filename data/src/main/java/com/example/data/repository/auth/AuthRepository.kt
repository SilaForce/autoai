package com.example.data.repository.auth

import com.example.data.datasource.remote.util.safeFirebaseCall
import com.example.data.mapper.toUser
import com.example.data.mapper.toUserDto
import com.example.data.mapper.toUserUpdateMap
import com.example.data.model.user.UserDto
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
                email = firebaseUser.email ?: email,
                createdAt = System.currentTimeMillis(),
            )

            firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .set(newUser.toUserDto())
                .await()

            newUser
        }
    }

    override suspend fun login(email: String, password: String): AppResult<User> {
        return safeFirebaseCall {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Firebase user is null after login")

            readUserDoc(firebaseUser.uid, firebaseUser.email ?: email)
        }
    }

    override suspend fun getCurrentUser(): AppResult<User> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return AppResult.Failure(DataError.Network.Unauthorized)

        return safeFirebaseCall {
            readUserDoc(firebaseUser.uid, firebaseUser.email.orEmpty())
        }
    }

    override suspend fun updateUser(user: User): AppResult<User> {
        return safeFirebaseCall {
            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .update(user.toUserUpdateMap())
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

            // DeleteUserUseCase has already cascaded the user-owned collections by this
            // point. This is the last step — once the auth record is gone, the client
            // loses credentials to query its own data.
            firebaseUser.delete().await()
        }
    }

    override suspend fun logout(): AppResult<Unit> {
        return safeFirebaseCall {
            firebaseAuth.signOut()
        }
    }

    private suspend fun readUserDoc(uid: String, email: String): User {
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .get()
            .await()

        val dto = snapshot.toObject(UserDto::class.java)?.let { current ->
            if (current.id.isBlank()) current.copy(id = uid) else current
        } ?: UserDto(id = uid)

        return dto.toUser(email = email)
    }

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}
