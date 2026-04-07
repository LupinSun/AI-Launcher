package de.mm20.launcher2.ai

import de.mm20.launcher2.ai.impl.AnthropicAiProvider
import de.mm20.launcher2.ai.impl.GeminiAiProvider
import de.mm20.launcher2.ai.impl.LocalAiProvider
import de.mm20.launcher2.ai.impl.OpenAiAiProvider
import de.mm20.launcher2.preferences.AiProviderType
import de.mm20.launcher2.preferences.ai.AiSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AiProviderManager(
    private val settings: AiSettings,
    private val keyStore: AiKeyStore,
    private val anthropic: AnthropicAiProvider,
    private val gemini: GeminiAiProvider,
    private val openAi: OpenAiAiProvider,
    private val local: LocalAiProvider,
) {
    val activeProvider: Flow<AiProvider> = settings.activeProvider.map { type ->
        when (type) {
            AiProviderType.Anthropic -> anthropic
            AiProviderType.Gemini -> gemini
            AiProviderType.OpenAI -> openAi
            AiProviderType.Local, AiProviderType.None -> local
        }
    }

    fun setApiKey(type: AiProviderType, key: String) = keyStore.setKey(type, key)
    fun getApiKey(type: AiProviderType) = keyStore.getKey(type)
    fun clearApiKey(type: AiProviderType) = keyStore.clearKey(type)
}
