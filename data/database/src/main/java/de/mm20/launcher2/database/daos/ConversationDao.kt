package de.mm20.launcher2.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.mm20.launcher2.database.entities.ConversationEntity
import de.mm20.launcher2.database.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Query("SELECT * FROM AiConversation ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM AiMessage WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessages(conversationId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM AiConversation WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("UPDATE AiConversation SET updatedAt = :timestamp WHERE id = :conversationId")
    suspend fun updateConversationTimestamp(conversationId: String, timestamp: Long)
}
