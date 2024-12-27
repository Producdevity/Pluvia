package com.OxGames.Pluvia.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.OxGames.Pluvia.data.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface SteamChatDao {
    @Insert
    suspend fun addMessage(message: ChatMessage)

    @Query("DELETE FROM chat_message WHERE friend_id = :friendId")
    suspend fun deleteMessages(friendId: Long)

    @Query("UPDATE chat_message SET is_unread = 1 WHERE friend_id = :friendId")
    suspend fun markAsRead(friendId: Long)

    @Query("SELECT * FROM chat_message WHERE friend_id = :friendId ORDER BY timestamp ASC")
    fun getMessages(friendId: Long): Flow<List<ChatMessage>>

    @Query("SELECT COUNT(*) FROM chat_message WHERE friend_id = :friendId AND is_unread = 1")
    fun getUnreadCount(friendId: Long): Flow<Int>

    @Query("DELETE FROM chat_message")
    fun deleteAllChats()
}
