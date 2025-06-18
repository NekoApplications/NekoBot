package icu.takeneko.nekobot.message

import icu.takeneko.nekobot.command.CommandManager
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.Message

data class CommandContextMiraiImpl(
    val platformMessage: Message,
    val group: Group?,
    val source: User,
    override val messageType: MessageType,
    val commandManager: CommandManager
) : CommandContext {
    override val messagePlain: String
        get() = platformMessage.contentToString()

    override fun describeSender() = source.id.toString()

    override fun describeGroup() = group!!.id.toString()

    override fun isGroupMessage() = messageType == MessageType.GROUP

    override fun isPrivateMessage() = messageType == MessageType.PRIVATE

    override fun manager(): CommandManager {
        return commandManager
    }
}