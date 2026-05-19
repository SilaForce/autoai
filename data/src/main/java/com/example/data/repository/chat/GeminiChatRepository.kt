package com.example.data.repository.chat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.data.BuildConfig
import com.example.data.datasource.remote.util.safeGenerativeAiCall
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.chat.ChatMessage
import com.example.domain.model.chat.ChatTool
import com.example.domain.model.chat.ChatToolParam
import com.example.domain.model.chat.ChatToolParamType
import com.example.domain.model.chat.MessageRole
import com.example.domain.repository.IAiChatRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.FunctionResponsePart
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.CancellationException
import org.json.JSONObject

class GeminiChatRepository : IAiChatRepository {

    companion object {
        private const val DEFAULT_MODEL = "gemini-2.5-flash"
        private const val MAX_TOOL_ITERATIONS = 4
    }

    override suspend fun sendMessage(
        prompt: String,
        history: List<ChatMessage>,
        systemInstruction: String,
        images: List<ByteArray>,
        tools: List<ChatTool>,
    ): AppResult<String> {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        val modelName = DEFAULT_MODEL

        if (apiKey.isBlank()) {
            println("[GeminiChatRepository] GEMINI_API_KEY is blank.")
            return AppResult.Failure(DataError.Network.Unauthorized)
        }

        val geminiTools = tools
            .takeIf { it.isNotEmpty() }
            ?.let { listOf(Tool(functionDeclarations = it.map(::toFunctionDeclaration))) }

        val generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            systemInstruction = content { text(systemInstruction) },
            tools = geminiTools,
        )

        val geminiHistory = mapHistoryToGeminiFormat(history)

        return safeGenerativeAiCall {
            val chatSession = generativeModel.startChat(history = geminiHistory)
            var response = if (images.isNotEmpty()) {
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

            var iterations = 0
            while (response.functionCalls.isNotEmpty() && iterations < MAX_TOOL_ITERATIONS) {
                val responseParts = response.functionCalls.map { call ->
                    val tool = tools.firstOrNull { it.name == call.name }
                    val resultText = if (tool == null) {
                        "Error: tool '${call.name}' is not available."
                    } else {
                        try {
                            tool.execute(call.args)
                        } catch (t: Throwable) {
                            if (t is CancellationException) throw t
                            "Error executing '${call.name}': ${t.message ?: "unknown failure"}"
                        }
                    }
                    FunctionResponsePart(call.name, JSONObject().put("result", resultText))
                }

                response = chatSession.sendMessage(
                    content(role = "function") {
                        responseParts.forEach { part(it) }
                    }
                )
                iterations++
            }

            response.text ?: ""
        }
    }

    private fun toFunctionDeclaration(tool: ChatTool): FunctionDeclaration {
        val schemas = tool.parameters.map(::toSchema)
        val required = tool.parameters.filter { it.required }.map { it.name }
        return FunctionDeclaration(
            name = tool.name,
            description = tool.description,
            parameters = schemas,
            requiredParameters = required,
        )
    }

    private fun toSchema(param: ChatToolParam): Schema<*> = when (param.type) {
        ChatToolParamType.STRING -> Schema.str(param.name, param.description)
        ChatToolParamType.LONG -> Schema.long(param.name, param.description)
        ChatToolParamType.INT -> Schema.int(param.name, param.description)
        ChatToolParamType.DOUBLE -> Schema.double(param.name, param.description)
        ChatToolParamType.BOOL -> Schema.bool(param.name, param.description)
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
