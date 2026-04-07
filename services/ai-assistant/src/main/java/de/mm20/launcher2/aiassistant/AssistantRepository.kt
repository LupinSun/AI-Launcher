package de.mm20.launcher2.aiassistant

import de.mm20.launcher2.ai.AiMessage
import de.mm20.launcher2.database.entities.ConversationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed interface AssistantState {
    data object Idle : AssistantState
    data object Thinking : AssistantState
    data class Streaming(val partialText: String) : AssistantState
    data class Error(val message: String) : AssistantState
}

interface AssistantRepository {
    val currentConversation: Flow<List<AiMessage>>
    val state: StateFlow<AssistantState>
    suspend fun sendMessage(text: String)
    suspend fun startNewConversation()
    fun getConversationHistory(): Flow<List<ConversationEntity>>
}
