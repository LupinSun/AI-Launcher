package de.mm20.launcher2.preferences

import kotlinx.serialization.Serializable

@Serializable
enum class AiProviderType {
    None, Anthropic, Gemini, OpenAI, Local
}
