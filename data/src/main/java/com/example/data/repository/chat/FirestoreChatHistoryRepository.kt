package com.example.data.repository.chat

import android.util.Log
import com.example.data.datasource.remote.util.safeFirebaseCall
import com.example.data.mapper.toChatMessage
import com.example.data.mapper.toChatMessageDto
import com.example.data.model.chat.ChatMessageDto
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.andThen
import com.example.domain.model.chat.ChatMessage
import com.example.domain.repository.IAiChatHistoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreChatHistoryRepository(
    private val firestore: FirebaseFirestore,
) : IAiChatHistoryRepository {

    override suspend fun saveMessage(message: ChatMessage, userId: String): AppResult<Unit> {
        val documentReference = firestore.collection(AI_CHAT_COLLECTION).document(message.id)
        return safeFirebaseCall {
            documentReference
                .set(message.toChatMessageDto(userId))
                .await()
            Unit
        }
    }

    override suspend fun deleteMessagesForThread(
        userId: String,
        threadId: String,
    ): AppResult<Unit> {
        return safeFirebaseCall {
            val snapshot = firestore.collection(AI_CHAT_COLLECTION)
                .whereEqualTo(FIELD_USER_ID, userId)
                .whereEqualTo(FIELD_THREAD_ID, threadId)
                .get()
                .await()

            // Firestore batches max 500 ops. Chunk to be safe for long histories.
            snapshot.documents.chunked(BATCH_SIZE_LIMIT).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }
            Unit
        }
    }

    override suspend fun loadHistory(
        userId: String,
        threadId: String,
        limit: Int,
    ): AppResult<List<ChatMessage>> {
        return safeFirebaseCall {
            firestore.collection(AI_CHAT_COLLECTION)
                .whereEqualTo(FIELD_USER_ID, userId)
                .whereEqualTo(FIELD_THREAD_ID, threadId)
                .orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
        }.andThen { querySnapshot ->
            val messages = mutableListOf<ChatMessage>()

            for (document in querySnapshot.documents) {
                val dto = document.toObject(ChatMessageDto::class.java)
                    ?.let { currentDto ->
                        if (currentDto.id.isBlank()) currentDto.copy(id = document.id)
                        else currentDto
                    }

                if (dto == null) {
                    Log.w(TAG, "Skipping malformed chat document: ${document.id}")
                    continue
                }

                when (val result = dto.toChatMessage()) {
                    is AppResult.Success -> messages.add(result.data)
                    is AppResult.Failure -> {
                        Log.w(TAG, "Skipping chat document with mapping error: ${document.id}")
                    }
                }
            }

            // Firestore returned DESC; chat UI expects chronological (oldest first).
            AppResult.Success(messages.sortedBy { it.timestamp })
        }
    }

    private companion object {
        const val TAG = "FirestoreChatHistory"
        const val AI_CHAT_COLLECTION = "aiChat"
        const val FIELD_USER_ID = "userId"
        const val FIELD_THREAD_ID = "threadId"
        const val FIELD_TIMESTAMP = "timestamp"
        const val BATCH_SIZE_LIMIT = 450
    }
}
