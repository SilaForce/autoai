package com.example.domain.usecase.chat

import com.example.domain.base.BaseUseCase
import com.example.domain.model.app.AppResult
import com.example.domain.model.app.DataError
import com.example.domain.model.chat.ChatMessage
import com.example.domain.repository.IAiChatHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher

data class LoadChatHistoryParams(
    val userId: String,
    val threadId: String,
    // Hard cap on how many messages get loaded into memory per thread. The AI only sends
    // the last 30 (AI_CONTEXT_MESSAGES in AiChatViewModel), so 200 is plenty for the user
    // to scroll back through recent history without ballooning the in-memory list when a
    // single thread has thousands of messages. Pagination would be the proper fix; this
    // is a pragmatic cap.
    val limit: Int = MAX_HISTORY_PAGE,
) {
    companion object {
        const val MAX_HISTORY_PAGE = 200
    }
}

class LoadChatHistoryUseCase(
    private val repository: IAiChatHistoryRepository,
    dispatcher: CoroutineDispatcher,
) : BaseUseCase<LoadChatHistoryParams, List<ChatMessage>>(dispatcher) {

    override suspend fun execute(params: LoadChatHistoryParams): AppResult<List<ChatMessage>> {
        if (params.userId.isBlank() || params.threadId.isBlank()) {
            return AppResult.Failure(DataError.Local.Validation.Generic)
        }
        // Clamp defensively in case a caller passes an oversized limit.
        val clampedLimit = params.limit.coerceIn(1, LoadChatHistoryParams.MAX_HISTORY_PAGE)
        return repository.loadHistory(
            userId = params.userId.trim(),
            threadId = params.threadId.trim(),
            limit = clampedLimit,
        )
    }
}
