package de.mm20.launcher2.aiassistant

import de.mm20.launcher2.ai.AiMessage
import de.mm20.launcher2.ai.AiProviderManager
import de.mm20.launcher2.database.AppDatabase
import de.mm20.launcher2.database.entities.ConversationEntity
import de.mm20.launcher2.database.entities.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

internal class AssistantRepositoryImpl(
    private val database: AppDatabase,
    private val providerManager: AiProviderManager,
    private val contextBuilder: ContextBuilder,
    private val actionDispatcher: ActionDispatcher,
) : AssistantRepository {

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val dao = database.conversationDao()

    private var activeConversationId: String = UUID.randomUUID().toString()

    private val _state = MutableStateFlow<AssistantState>(AssistantState.Idle)
    override val state: StateFlow<AssistantState> = _state.asStateFlow()

    override val currentConversation: Flow<List<AiMessage>> =
        dao.getMessages(activeConversationId).map { entities ->
            entities.map { AiMessage(roleFromString(it.role), it.content) }
        }

    override suspend fun sendMessage(text: String) {
        if (_state.value !is AssistantState.Idle) return

        // Persist user message
        val userMsg = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = activeConversationId,
            role = "user",
            content = text,
            timestamp = System.currentTimeMillis(),
        )
        ensureConversationExists()
        dao.insertMessage(userMsg)
        dao.updateConversationTimestamp(activeConversationId, System.currentTimeMillis())

        _state.value = AssistantState.Thinking

        scope.launch {
            try {
                val provider = providerManager.activeProvider.firstOrNull()
                if (provider == null || !provider.isConfigured) {
                    _state.value = AssistantState.Error("No AI provider configured")
                    return@launch
                }

                // Build message history for the API call
                val history = dao.getMessages(activeConversationId).firstOrNull() ?: emptyList()
                val systemContext = contextBuilder.buildSystemContext()
                val messages = buildList {
                    add(AiMessage(
                        role = AiMessage.Role.System,
                        content = "You are a helpful assistant embedded in an Android launcher. " +
                            "Answer concisely. You can perform actions using JSON tool calls.\n\n" +
                            actionDispatcher.toolDescriptions() + "\n\nContext:\n" + systemContext,
                    ))
                    addAll(history.map { AiMessage(roleFromString(it.role), it.content) })
                }

                val responseBuffer = StringBuilder()
                provider.chat(messages).collect { token ->
                    responseBuffer.append(token)
                    _state.value = AssistantState.Streaming(responseBuffer.toString())
                }

                val fullResponse = responseBuffer.toString()

                // Try dispatching as a tool call; if dispatched, don't save as text
                val wasAction = actionDispatcher.tryDispatch(fullResponse)

                val assistantContent = if (wasAction) "[Action executed]" else fullResponse

                val assistantMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    conversationId = activeConversationId,
                    role = "assistant",
                    content = assistantContent,
                    timestamp = System.currentTimeMillis(),
                    functionCall = if (wasAction) fullResponse else null,
                )
                dao.insertMessage(assistantMsg)
                dao.updateConversationTimestamp(activeConversationId, System.currentTimeMillis())
                _state.value = AssistantState.Idle

            } catch (e: Exception) {
                _state.value = AssistantState.Error(e.message ?: "Unknown error")
            }
        }
    }

    override suspend fun startNewConversation() {
        activeConversationId = UUID.randomUUID().toString()
        _state.value = AssistantState.Idle
    }

    override fun getConversationHistory(): Flow<List<ConversationEntity>> =
        dao.getAllConversations()

    private suspend fun ensureConversationExists() {
        val existing = dao.getAllConversations().firstOrNull()
            ?.firstOrNull { it.id == activeConversationId }
        if (existing == null) {
            dao.insertConversation(
                ConversationEntity(
                    id = activeConversationId,
                    title = "New conversation",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            )
        }
    }

    private fun roleFromString(role: String): AiMessage.Role = when (role) {
        "system" -> AiMessage.Role.System
        "assistant" -> AiMessage.Role.Assistant
        else -> AiMessage.Role.User
    }
}
