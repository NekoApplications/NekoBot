package icu.takeneko.nekobot

import com.github.kotlintelegrambot.dispatcher.handlers.TextHandlerEnvironment
import icu.takeneko.nekobot.command.CommandManager
import icu.takeneko.nekobot.message.CommandContext
import icu.takeneko.nekobot.message.MessageType

data class CommandContextTelegramImpl(
    val platformMessage: TextHandlerEnvironment,
    val manager: CommandManager,
    override val messagePlain: String = platformMessage.text,
    override val messageType: MessageType = if (platformMessage.message.chat.type == "private") {
        MessageType.PRIVATE
    } else {
        MessageType.GROUP

    }
) : CommandContext {
    override fun describeSender(): String =
        platformMessage.message.from?.id?.toString()
            ?: throw IllegalArgumentException("Could not describe sender from ${platformMessage.message.from}.")

    override fun describeGroup(): String = platformMessage.message.chat.id.toString()

    override fun isGroupMessage(): Boolean = messageType == MessageType.GROUP

    override fun isPrivateMessage(): Boolean = !isGroupMessage()

    override fun manager(): CommandManager = manager
}