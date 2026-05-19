package com.example.data.repository.chat

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
import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.FunctionDeclaration
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.Part
import com.google.genai.types.Schema
import com.google.genai.types.Tool
import kotlinx.coroutines.CancellationException

class GeminiChatRepository : IAiChatRepository {

    companion object {
        private const val DEFAULT_MODEL = "gemini-2.5-flash"
        private const val MAX_TOOL_ITERATIONS = 4
        private const val IMAGE_MIME = "image/jpeg"
    }

    private val client: Client by lazy {
        Client.builder().apiKey(BuildConfig.GEMINI_API_KEY.trim()).build()
    }

    override suspend fun sendMessage(
        prompt: String,
        history: List<ChatMessage>,
        systemInstruction: String,
        images: List<ByteArray>,
        tools: List<ChatTool>,
    ): AppResult<String> {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isBlank()) {
            println("[GeminiChatRepository] GEMINI_API_KEY is blank.")
            return AppResult.Failure(DataError.Network.Unauthorized)
        }

        val configBuilder = GenerateContentConfig.builder()
            .systemInstruction(Content.fromParts(Part.fromText(systemInstruction)))
        if (tools.isNotEmpty()) {
            configBuilder.tools(listOf(buildGeminiTool(tools)))
        }
        val config = configBuilder.build()

        val contents = mutableListOf<Content>().apply {
            addAll(mapHistory(history))
            add(buildUserContent(prompt, images))
        }

        return safeGenerativeAiCall {
            var response = client.models.generateContent(DEFAULT_MODEL, contents, config)
            var iterations = 0
            while (!response.functionCalls().isNullOrEmpty() && iterations < MAX_TOOL_ITERATIONS) {
                response.candidates().orElse(emptyList())
                    .firstOrNull()
                    ?.content()
                    ?.orElse(null)
                    ?.let { contents += it }

                val funcResponseParts = response.functionCalls()!!.map { call ->
                    val callName = call.name().orElse("")
                    val callArgs = call.args().orElse(emptyMap())
                    val tool = tools.firstOrNull { it.name == callName }
                    val resultText = if (tool == null) {
                        "Error: tool '$callName' is not available."
                    } else {
                        try {
                            tool.execute(callArgs.mapValues { entry -> entry.value?.toString() })
                        } catch (t: Throwable) {
                            if (t is CancellationException) throw t
                            "Error executing '$callName': ${t.message ?: "unknown failure"}"
                        }
                    }
                    Part.fromFunctionResponse(callName, mapOf<String, Any>("result" to resultText))
                }
                contents += Content.builder()
                    .role("user")
                    .parts(funcResponseParts)
                    .build()

                response = client.models.generateContent(DEFAULT_MODEL, contents, config)
                iterations++
            }
            response.text() ?: ""
        }
    }

    private fun buildGeminiTool(tools: List<ChatTool>): Tool {
        val declarations = tools.map { tool ->
            val properties: Map<String, Schema> = tool.parameters.associate { param ->
                param.name to toSchema(param)
            }
            val required = tool.parameters.filter { it.required }.map { it.name }
            val paramsSchema = Schema.builder()
                .type("OBJECT")
                .properties(properties)
                .required(required)
                .build()
            FunctionDeclaration.builder()
                .name(tool.name)
                .description(tool.description)
                .parameters(paramsSchema)
                .build()
        }
        return Tool.builder().functionDeclarations(declarations).build()
    }

    private fun toSchema(param: ChatToolParam): Schema = when (param.type) {
        ChatToolParamType.STRING -> Schema.builder()
            .type("STRING")
            .description(param.description)
            .build()
        ChatToolParamType.LONG,
        ChatToolParamType.INT -> Schema.builder()
            .type("INTEGER")
            .description(param.description)
            .build()
        ChatToolParamType.DOUBLE -> Schema.builder()
            .type("NUMBER")
            .description(param.description)
            .build()
        ChatToolParamType.BOOL -> Schema.builder()
            .type("BOOLEAN")
            .description(param.description)
            .build()
        ChatToolParamType.ENUM -> {
            val values = param.values
            val builder = Schema.builder()
                .type("STRING")
                .description(param.description)
            if (!values.isNullOrEmpty()) builder.enum_(values)
            builder.build()
        }
    }

    private fun buildUserContent(prompt: String, images: List<ByteArray>): Content {
        val parts = mutableListOf<Part>()
        images.forEach { bytes -> parts += Part.fromBytes(bytes, IMAGE_MIME) }
        parts += Part.fromText(prompt)
        return Content.builder().role("user").parts(parts).build()
    }

    private fun mapHistory(history: List<ChatMessage>): List<Content> {
        val sanitizedHistory = history
            .filter { it.text.isNotBlank() }
            .dropWhile { it.role == MessageRole.AI }

        return sanitizedHistory.map { message ->
            val parts = mutableListOf<Part>()
            message.images.forEach { bytes -> parts += Part.fromBytes(bytes, IMAGE_MIME) }
            parts += Part.fromText(message.text)
            Content.builder()
                .role(if (message.role == MessageRole.USER) "user" else "model")
                .parts(parts)
                .build()
        }
    }
}
