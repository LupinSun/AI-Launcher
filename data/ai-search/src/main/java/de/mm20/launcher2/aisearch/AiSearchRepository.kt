package de.mm20.launcher2.aisearch

import de.mm20.launcher2.ai.AiMessage
import de.mm20.launcher2.ai.AiProviderManager
import de.mm20.launcher2.preferences.ai.AiSettings
import de.mm20.launcher2.search.SearchableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

internal class AiSearchRepository(
    private val providerManager: AiProviderManager,
    private val settings: AiSettings,
) : SearchableRepository<AiSearchResult> {

    override fun search(query: String, allowNetwork: Boolean): Flow<List<AiSearchResult>> = flow {
        // Skip short queries
        if (query.length < 5) {
            emit(emptyList())
            return@flow
        }

        val aiSearchEnabled = settings.aiSearchEnabled.firstOrNull() ?: false
        if (!aiSearchEnabled) {
            emit(emptyList())
            return@flow
        }

        // Let local results appear first
        emit(emptyList())

        if (!allowNetwork) return@flow

        val provider = providerManager.activeProvider.firstOrNull() ?: return@flow
        if (!provider.isConfigured) return@flow

        val messages = listOf(
            AiMessage(
                role = AiMessage.Role.System,
                content = "You are a concise assistant embedded in a phone launcher search bar. " +
                    "Answer factual questions in 2-3 sentences. " +
                    "If the user input is NOT a question or request, respond with exactly: NOT_A_QUESTION",
            ),
            AiMessage(role = AiMessage.Role.User, content = query),
        )

        val tokenStream = provider.chat(messages)
        val buffer = StringBuilder()

        tokenStream.collect { token ->
            buffer.append(token)
            val partial = buffer.toString()
            if (partial.trimStart().startsWith("NOT_A_QUESTION")) {
                return@collect
            }
            emit(listOf(AiSearchResult(query = query, answer = partial)))
        }

        // Final check: if the complete answer is NOT_A_QUESTION, emit empty
        if (buffer.toString().trim() == "NOT_A_QUESTION") {
            emit(emptyList())
        }
    }
}
