package de.mm20.launcher2.ai

import kotlinx.coroutines.flow.Flow

interface AiProvider {
    /**
     * Send messages and receive a streaming response (emits tokens as they arrive).
     */
    suspend fun chat(messages: List<AiMessage>): Flow<String>

    /**
     * Quick intent classification — returns one of the provided labels or null.
     */
    suspend fun classify(text: String, labels: List<String>): String?

    val isConfigured: Boolean
}
