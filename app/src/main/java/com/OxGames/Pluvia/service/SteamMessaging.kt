package com.OxGames.Pluvia.service

import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.utils.logI
import com.OxGames.Pluvia.utils.logW
import `in`.dragonbra.javasteam.enums.EChatEntryType
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.CFriendMessages_AckMessage_Notification
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.CFriendMessages_GetRecentMessages_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.CFriendMessages_IncomingMessage_Notification
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.CFriendMessages_MessageReaction_Notification
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.CFriendMessages_SendMessage_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.CFriendMessages_UpdateMessageReaction_Request
import `in`.dragonbra.javasteam.rpc.service.FriendMessages
import `in`.dragonbra.javasteam.rpc.service.FriendMessagesClient
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodNotification
import `in`.dragonbra.javasteam.types.SteamID
import java.io.Closeable

typealias IncomingMessageProto = CFriendMessages_IncomingMessage_Notification.Builder
typealias AckMessageProto = CFriendMessages_AckMessage_Notification.Builder
typealias MessageReactionProto = CFriendMessages_MessageReaction_Notification.Builder
typealias ReactionType = SteammessagesFriendmessagesSteamclient.EMessageReactionType

/**
 * This class should be responsible for Chat Related messages with friends.
 */
class SteamMessaging(
    private val service: SteamService,
) : AutoCloseable {
    private val subscriptions: ArrayList<Closeable> = ArrayList()

    private var friendMessages: FriendMessages? = service._unifiedMessages?.createService<FriendMessages>()

    // private var friendMessagesClient: FriendMessagesClient? = service._unifiedMessages?.createService<FriendMessagesClient>()

    init {
        with(service._callbackManager!!) {
            subscribeServiceNotification<FriendMessagesClient, IncomingMessageProto> {
                onIncomingMessage(it)
            }.also(subscriptions::add)
            subscribeServiceNotification<FriendMessagesClient, AckMessageProto> {
                onNotifyAckMessageEcho(it)
            }.also(subscriptions::add)
            subscribeServiceNotification<FriendMessagesClient, MessageReactionProto> {
                onMessageReaction(it)
            }.also(subscriptions::add)
        }
    }

    override fun close() {
        subscriptions.forEach { it.close() }
        subscriptions.clear()

        friendMessages = null
    }

    // region [REGION] FriendMessages
    suspend fun getRecentMessages(steamID: SteamID, msgCount: Int = 50) {
        val request = CFriendMessages_GetRecentMessages_Request.newBuilder().apply {
            steamid1 = service._steamClient!!.steamID.convertToUInt64()
            steamid2 = steamID.convertToUInt64()
            count = msgCount
            rtime32StartTime = 0
            bbcodeFormat = true
            startOrdinal = 0
            timeLast = Int.MAX_VALUE // More explicit than magic number // TODO what did I mean by this.
            ordinalLast = 0
        }.build()

        val response = friendMessages?.getRecentMessages(request)?.toDeferred()?.await()

        if (response == null || response.result != EResult.OK) {
            logW("Failed to get message history for ${steamID.convertToUInt64()}")
            return
        }

        TODO("insert into database, while being careful of dupes.")
    }

    fun getActiveMessageSessions() {
        TODO("What is this function for")
    }

    suspend fun setTyping(steamID: SteamID) {
        logI("Sending typing status to ${steamID.convertToUInt64()}")
        val request = CFriendMessages_SendMessage_Request.newBuilder().apply {
            steamid = steamID.convertToUInt64()
            chatEntryType = EChatEntryType.Typing.code()
        }.build()

        val response = friendMessages?.sendMessage(request)?.toDeferred()?.await()

        if (response == null || response.result != EResult.OK) {
            logW("Failed to send message")
            return
        }

        // TODO we get 'server_timestamp' back.
    }

    suspend fun sendMessage(
        steamID: SteamID,
        chatMessage: String,
    ) {
        val trimmedMessage = chatMessage.trim() // .replace('\u02D0', ':')

        val request = CFriendMessages_SendMessage_Request.newBuilder().apply {
            steamid = steamID.convertToUInt64()
            chatEntryType = EChatEntryType.ChatMsg.code()
            message = trimmedMessage
            containsBbcode = true
            /* clientMessageId */
            // TODO this is in NHA now, what is it?
        }.build()

        val response = friendMessages?.sendMessage(request)?.toDeferred()?.await()

        if (response == null || response.result != EResult.OK) {
            logW("Failed to send message")
            return
        }

        TODO("'response' returns 'server_timestamp'")
        TODO("Insert message into database")
    }

    fun ackMessage(steamID: SteamID) {
        logI("Acking Message for ${steamID.convertToUInt64()}")

        val request = CFriendMessages_AckMessage_Notification.newBuilder().apply {
            steamidPartner = steamID.convertToUInt64()
            timestamp = System.currentTimeMillis().div(1000).toInt()
        }.build()

        friendMessages?.ackMessage(request)
    }

    // fun isInFriendsUIBeta()

    suspend fun updateMessageReaction(
        steamID: SteamID,
        timestamp: Int,
        isEmoticon: Boolean,
        msgReaction: String,
        isAdding: Boolean,
    ) {
        if (!msgReaction.startsWith(':') && !msgReaction.endsWith(":")) {
            // TODO should compare this stickers/emotes we own.
            logW("Invalid reaction to send")
            return
        }

        val request = CFriendMessages_UpdateMessageReaction_Request.newBuilder().apply {
            val type = if (isEmoticon) {
                ReactionType.k_EMessageReactionType_Emoticon
            } else {
                ReactionType.k_EMessageReactionType_Sticker
            }

            steamid = steamID.convertToUInt64()
            serverTimestamp = timestamp
            ordinal = 0
            reactionType = type
            reaction = msgReaction
            isAdd = isAdding
        }.build()

        val response = friendMessages?.updateMessageReaction(request)?.toDeferred()?.await()

        if (response == null || response.result != EResult.OK) {
            logW("Unable to update message reaction")
            return
        }

        TODO("response sends back a list of reactors (aka steam id 3's")
    }
    // endregion

    // region [REGION] FriendMessagesClient
    private fun onIncomingMessage(notification: ServiceMethodNotification<IncomingMessageProto>) {
        logI("Incoming Message...")
        TODO()
    }

    private fun onNotifyAckMessageEcho(notification: ServiceMethodNotification<AckMessageProto>) {
        logI("Ack Message Echo")
        TODO()
    }

    private fun onMessageReaction(notification: ServiceMethodNotification<MessageReactionProto>) {
        logI("Message Reaction")
        TODO()
    }
    // endregion
}
