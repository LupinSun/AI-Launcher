package de.mm20.launcher2.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiMessage(
    val role: Role,
    val content: String,
) {
    @Serializable
    enum class Role {
        @SerialName("system") System,
        @SerialName("user") User,
        @SerialName("assistant") Assistant
    }
}

val AiMessage.Role.apiName: String get() = when(this) {
    AiMessage.Role.System -> "system"
    AiMessage.Role.User -> "user"
    AiMessage.Role.Assistant -> "assistant"
}
