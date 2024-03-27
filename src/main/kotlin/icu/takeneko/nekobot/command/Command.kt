package icu.takeneko.nekobot.command

import icu.takeneko.nekobot.message.Message

abstract class Command {

    open val commandPrefix: String = "!"
    open val helpMessage: String = ""
    abstract fun handle(commandMessage: CommandMessage): Message
    operator fun invoke(commandMessage: CommandMessage) = handle(commandMessage)

}

class CommandMessage(message: Message) {
    private val component = message.messagePlain.split(" ")
    val args = component.subList(1, component.size)
    val commandPrefix = component[0]
    val scene = message.scene
    val from = message.messageType
    operator fun get(index: Int): String? {
        return try {
            args[index]
        } catch (_: Exception) {
            null
        }
    }

    operator fun <T> invoke(fn: CommandMessage.() -> T): T {
        return fn(this)
    }

}