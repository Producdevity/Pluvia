package com.OxGames.Pluvia.service

import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.utils.logI
import `in`.dragonbra.javasteam.enums.EChatEntryType
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.CFriendMessages_IncomingMessage_Notification
import `in`.dragonbra.javasteam.rpc.service.FriendMessages
import `in`.dragonbra.javasteam.rpc.service.FriendMessagesClient
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodNotification
import java.io.Closeable

typealias CFriendMessages = CFriendMessages_IncomingMessage_Notification
typealias CFriendMessagesBuilder = CFriendMessages_IncomingMessage_Notification.Builder

/**
 * This class should be responsible for Chat Related messages with friends.
 */
class SteamMessaging(
    private val service: SteamService,
) : AutoCloseable {
    private val subscriptions: ArrayList<Closeable> = ArrayList()

    private var friendMessages: FriendMessages? = null

    init {
        friendMessages = service._unifiedMessages?.createService<FriendMessages>()

        service._callbackManager?.subscribeServiceNotification<FriendMessagesClient, CFriendMessagesBuilder> {
            onIncomingMessage(it)
        }?.also {
            subscriptions.add(it)
        }
    }

    override fun close() {
        subscriptions.forEach { it.close() }

        friendMessages = null
    }

    private fun onIncomingMessage(notification: ServiceMethodNotification<CFriendMessages_IncomingMessage_Notification.Builder>) {
        logI("Incoming Message...")
        when (EChatEntryType.from(notification.body.chatEntryType)) {
            EChatEntryType.ChatMsg -> onChatMessage(notification.body.build())
            EChatEntryType.Typing -> onTyping(notification.body.build())
            else -> Unit // Don't care about the others right now.
        }
    }

    private fun onChatMessage(notification: CFriendMessages_IncomingMessage_Notification) {
        service.chatDao
    }

    private fun onTyping(notification: CFriendMessages_IncomingMessage_Notification) {
        TODO("Not yet implemented")
    }
}
