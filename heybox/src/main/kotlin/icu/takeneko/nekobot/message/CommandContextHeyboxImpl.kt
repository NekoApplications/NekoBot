package icu.takeneko.nekobot.message

import icu.takeneko.nekobot.command.CommandManager
import net.cjsah.bot.event.events.CommandEvent

class CommandContextHeyboxImpl(val event: CommandEvent, val manager: CommandManager) : CommandContext {
    override val messagePlain: String = buildString {
        val command = event.commandInfo
        append(command.command)
        append(" ")
        append(command.options["args"])
    }
    override val messageType: MessageType
        get() = MessageType.GROUP

    override fun describeSender(): String {
        return event.senderInfo.id.toString()
    }

    override fun describeGroup(): String {
        return event.channelInfo.id
    }

    override fun isGroupMessage(): Boolean = true

    override fun isPrivateMessage(): Boolean = false

    override fun manager(): CommandManager = manager
}