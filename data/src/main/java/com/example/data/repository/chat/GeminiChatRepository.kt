package com.example.data.repository.chat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.data.BuildConfig
import com.example.data.datasource.remote.util.safeGenerativeAiCall
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.chat.ChatMessage
import com.example.domain.model.chat.MessageRole
import com.example.domain.repository.IAiChatRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content

class GeminiChatRepository : IAiChatRepository {

companion object {
        private const val DEFAULT_MODEL = "gemini-2.5-flash"
    }

    override suspend fun sendMessage(
        prompt: String,
        history: List<ChatMessage>,
        systemInstruction: String,
        images: List<ByteArray>
    ): AppResult<String> {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        val modelName = DEFAULT_MODEL

        if (apiKey.isBlank()) {
            println("[GeminiChatRepository] GEMINI_API_KEY is blank.")
            return AppResult.Failure(DataError.Network.Unauthorized)
        }

        val generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            systemInstruction = content { text(systemInstruction) }
        )

        val geminiHistory = mapHistoryToGeminiFormat(history)

        return safeGenerativeAiCall {
            val chatSession = generativeModel.startChat(history = geminiHistory)
            val response = if (images.isNotEmpty()) {
                chatSession.sendMessage(content {
                    images.forEach { bytes ->
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        image(bitmap)
                    }
                    text(prompt)
                })
            } else {
                chatSession.sendMessage(prompt)
            }
            response.text ?: ""
        }
    }

    private fun mapHistoryToGeminiFormat(history: List<ChatMessage>): List<Content> {
        val sanitizedHistory = history
            .filter { it.text.isNotBlank() }
            .dropWhile { it.role == MessageRole.AI }

        return sanitizedHistory.map { message ->
            content(role = if (message.role == MessageRole.USER) "user" else "model") {
                message.images.forEach { bytes ->
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    image(bitmap)
                }
                text(message.text)
            }
        }
    }
}