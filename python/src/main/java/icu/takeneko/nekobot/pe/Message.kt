package icu.takeneko.nekobot.pe

import icu.takeneko.nekobot.command.CommandManager
import icu.takeneko.nekobot.message.CommandContext
import icu.takeneko.nekobot.message.MessageType

data class Message(
    val senderInfo: String,
    val channelInfo: String,
    override val messageType: MessageType,
    val manager: CommandManager,
    override val messagePlain: String
) : CommandContext {

    override fun describeSender(): String {
        return senderInfo
    }

    override fun describeGroup(): String {
        return channelInfo
    }

    override fun isGroupMessage(): Boolean = messageType == MessageType.GROUP

    override fun isPrivateMessage(): Boolean = messageType == MessageType.PRIVATE

    override fun manager(): CommandManager = manager
}