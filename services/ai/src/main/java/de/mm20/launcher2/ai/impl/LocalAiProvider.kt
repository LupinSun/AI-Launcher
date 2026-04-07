package de.mm20.launcher2.ai.impl

import de.mm20.launcher2.ai.AiMessage
import de.mm20.launcher2.ai.AiProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Stub for future on-device LLM support (e.g. Gemini Nano on Android 15+).
 * Currently returns empty results.
 */
class LocalAiProvider : AiProvider {
    override val isConfigured: Boolean = false
    override suspend fun chat(messages: List<AiMessage>): Flow<String> = emptyFlow()
    override suspend fun classify(text: String, labels: List<String>): String? = null
}
