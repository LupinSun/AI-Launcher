package de.mm20.launcher2.aisuggestions

import kotlinx.serialization.Serializable

@Serializable
data class SuggestedItem(
    val key: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis(),
)
