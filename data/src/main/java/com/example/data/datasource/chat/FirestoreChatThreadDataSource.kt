package com.example.data.datasource.chat

import android.util.Log
import com.example.data.datasource.remote.util.safeFirebaseCall
import com.example.data.mapper.toChatThread
import com.example.data.mapper.toChatThreadDto
import com.example.data.model.chat.ChatThreadDto
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.andThen
import com.example.domain.model.chat.ChatThread
import com.example.domain.datasource.AiChatThreadDataSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreChatThreadDataSource(
    private val firestore: FirebaseFirestore,
) : AiChatThreadDataSource {

    override suspend fun createThread(thread: ChatThread): AppResult<ChatThread> {
        val documentReference = firestore.collection(AI_CHAT_THREADS_COLLECTION).document(thread.id)
        return safeFirebaseCall {
            documentReference
                .set(thread.toChatThreadDto())
                .await()
            thread
        }
    }

    /**
     * Partial update — only [ChatThread.title] and [ChatThread.updatedAt] are written. The original
     * [ChatThread.createdAt] and [ChatThread.userId] in Firestore are preserved, so callers don't
     * need to know the existing values to bump the timestamp.
     */
    override suspend fun updateThread(thread: ChatThread): AppResult<Unit> {
        val documentReference = firestore.collection(AI_CHAT_THREADS_COLLECTION).document(thread.id)
        return safeFirebaseCall {
            documentReference.update(
                mapOf(
                    FIELD_TITLE to thread.title,
                    FIELD_UPDATED_AT to thread.updatedAt,
                )
            ).await()
            Unit
        }
    }

    override suspend fun deleteThread(threadId: String): AppResult<Unit> {
        val documentReference = firestore.collection(AI_CHAT_THREADS_COLLECTION).document(threadId)
        return safeFirebaseCall {
            documentReference.delete().await()
            Unit
        }
    }

    override suspend fun deleteAllForUser(userId: String): AppResult<Unit> {
        return safeFirebaseCall {
            val snapshot = firestore.collection(AI_CHAT_THREADS_COLLECTION)
                .whereEqualTo(FIELD_USER_ID, userId)
                .get()
                .await()

            // Firestore batches max 500 ops; chunk for safety.
            snapshot.documents.chunked(BATCH_LIMIT).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }
            Unit
        }
    }

    override suspend fun loadThreads(userId: String): AppResult<List<ChatThread>> {
        return safeFirebaseCall {
            firestore.collection(AI_CHAT_THREADS_COLLECTION)
                .whereEqualTo(FIELD_USER_ID, userId)
                .orderBy(FIELD_UPDATED_AT, Query.Direction.DESCENDING)
                .get()
                .await()
        }.andThen { querySnapshot ->
            val threads = mutableListOf<ChatThread>()

            for (document in querySnapshot.documents) {
                val dto = document.toObject(ChatThreadDto::class.java)
                    ?.let { currentDto ->
                        if (currentDto.id.isBlank()) currentDto.copy(id = document.id)
                        else currentDto
                    }

                if (dto == null) {
                    Log.w(TAG, "Skipping malformed thread document: ${document.id}")
                    continue
                }

                when (val result = dto.toChatThread()) {
                    is AppResult.Success -> threads.add(result.data)
                    is AppResult.Failure -> {
                        Log.w(TAG, "Skipping thread document with mapping error: ${document.id}")
                    }
                }
            }

            AppResult.Success(threads)
        }
    }

    private companion object {
        const val TAG = "FirestoreChatThread"
        const val AI_CHAT_THREADS_COLLECTION = "aiChatThreads"
        const val FIELD_USER_ID = "userId"
        const val FIELD_TITLE = "title"
        const val FIELD_UPDATED_AT = "updatedAt"
        const val BATCH_LIMIT = 500
    }
}
