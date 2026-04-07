package de.mm20.launcher2.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AiConversation")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
)
