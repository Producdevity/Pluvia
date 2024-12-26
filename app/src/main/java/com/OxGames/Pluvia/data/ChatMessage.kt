package com.OxGames.Pluvia.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import `in`.dragonbra.javasteam.enums.EChatEntryType
import `in`.dragonbra.javasteam.types.SteamID

@Entity("chat_message")
data class ChatMessage(
    /* Common */
    @PrimaryKey val id: Long = SteamID().convertToUInt64(),
    @ColumnInfo("is_sender") val isSender: Boolean = false,
    @ColumnInfo("chat_entry_type") val chatEntryType: EChatEntryType = EChatEntryType.Invalid,
    @ColumnInfo("message") val message: String = "",
    @ColumnInfo("low_priority") val lowPriority: Boolean = false,
    @ColumnInfo("ordinal") val ordinal: Int = 0,

    /* Incoming Messages */
    @ColumnInfo("from_limited_account") val fromLimitedAccount: Boolean = false,
    @ColumnInfo("rtime32_server_timestamp") val rTime32: String = "",
    @ColumnInfo("local_echo") val localEcho: Boolean = false,
    @ColumnInfo("message_no_bbcode") val msgNoBBCode: String = "",

    /* Outgoing Messages */
    @ColumnInfo("modified_message") val modifiedMessage: Boolean = false, // Incoming
    @ColumnInfo("server_timestamp") val serverTimestamp: Boolean = false, // Incoming
    @ColumnInfo("message_without_bb_code") val messageWithoutBBCode: Boolean = false, // Incoming
    @ColumnInfo("contains_bbcode") val containsBBCode: Boolean = false, // Outgoing
    @ColumnInfo("echo_to_sender") val echoToSender: Boolean = false, // Outgoing
    @ColumnInfo("client_message_id") val clientMessageId: String = "", // Outgoing
)
