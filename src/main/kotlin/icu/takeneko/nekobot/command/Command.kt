package icu.takeneko.nekobot.command

import icu.takeneko.nekobot.message.Message
import icu.takeneko.nekobot.message.MessageResponse

abstract class Command {

    open val commandPrefix: String = "!"
    open val helpMessage: String = ""
    abstract fun handle(commandMessage: CommandMessage): MessageResponse?
    operator fun invoke(commandMessage: CommandMessage) = handle(commandMessage)

}

class CommandMessage(val message: Message) {
    private val component = message.messagePlain.split(" ")
    val args = component.subList(1, component.size)
    val commandPrefix = component[0]
    val sender = message.describeSender()
    val group = message.group?.id?.toString()
    val from = message.messageType
    operator fun get(index: Int): String? {
        return try {
            args[index]
        } catch (_: Exception) {
            null
        }
    }

    fun createResponse(): MessageResponse {
        return MessageResponse(message)
    }

    fun createResponse(fn: MessageResponse.() -> Unit): MessageResponse {
        return MessageResponse(message).apply(fn)
    }

    operator fun <T> invoke(fn: CommandMessage.() -> T): T {
        return fn(this)
    }

}