package com.OxGames.Pluvia.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import `in`.dragonbra.javasteam.enums.EChatEntryType
import `in`.dragonbra.javasteam.types.SteamID

/**
 * Represents a chat message
 * @param friendId The [SteamID] of the friend we're talking to.
 * @param userId The [SteamID] of the logged in account.
 * @param chatEntryType The type of message, see [EChatEntryType]
 * @param isUnread Whether the message is unread or not.
 * @param sentByUser Whether the message was sent by the logged in user.
 * @param message The chat message.
 * @param timestamp The timestamp of the message.
 */
@Entity("chat_message")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("friend_id") val friendId: Long = 0L,
    @ColumnInfo("user_id") val userId: Long = 0L,
    @ColumnInfo("chat_entry_type") val chatEntryType: EChatEntryType = EChatEntryType.Invalid,
    @ColumnInfo("is_unread") val isUnread: Boolean = false,
    @ColumnInfo("sent_by_user") val sentByUser: Boolean = false,
    @ColumnInfo("message") val message: String = "",
    @ColumnInfo("timestamp") val timestamp: Long = 0L,
)
