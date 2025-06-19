package icu.takeneko.nekobot.message

import icu.takeneko.nekobot.command.CommandManager
import net.cjsah.bot.event.events.CommandEvent
import net.cjsah.bot.event.events.MessageEvent

class CommandContextHeyboxImpl(
    val senderInfo: String,
    val channelInfo: String,
    val manager: CommandManager,
    override val messagePlain: String
) : CommandContext {

    override val messageType: MessageType
        get() = MessageType.GROUP

    constructor(event: CommandEvent, manager: CommandManager) : this(
        event.senderInfo.id.toString(),
        event.channelInfo.id,
        manager,
        buildString {
            val command = event.commandInfo
            append(command.command)
            append(" ")
            append(command.options["args"])
        }
    )

    constructor(event: MessageEvent, manager: CommandManager) : this(
        event.userId.toString(),
        event.channelId.toString(),
        manager,
        event.msg
    )

    override fun describeSender(): String {
        return senderInfo
    }

    override fun describeGroup(): String {
        return channelInfo
    }

    override fun isGroupMessage(): Boolean = true

    override fun isPrivateMessage(): Boolean = false

    override fun manager(): CommandManager = manager
}