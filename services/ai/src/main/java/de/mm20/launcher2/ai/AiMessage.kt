package de.mm20.launcher2.ai

import kotlinx.serialization.Serializable

@Serializable
data class AiMessage(
    val role: Role,
    val content: String,
) {
    @Serializable
    enum class Role { system, user, assistant }
}
