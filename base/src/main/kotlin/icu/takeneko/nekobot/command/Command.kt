package icu.takeneko.nekobot.command

import icu.takeneko.nekobot.config.config
import icu.takeneko.nekobot.message.CommandContext
import icu.takeneko.nekobot.message.builder.MessageCreator
import icu.takeneko.nekobot.message.MessageType
import java.util.regex.Pattern

abstract class Command {
    open val commandPrefix: String = "!"
    open val helpMessage: String = ""
    abstract fun handle(commandMessage: CommandMessage): MessageCreator
    operator fun invoke(commandMessage: CommandMessage) = handle(commandMessage)
}

class CommandMessage(val context: CommandContext) {
    private val component = context.messagePlain.split(regex)
    val args = component.subList(1, component.size)
    val commandPrefix = component[0]

    operator fun get(index: Int): String? {
        return try {
            args[index]
        } catch (_: Exception) {
            null
        }
    }

    fun createResponse(fn: MessageCreator.() -> Unit): MessageCreator {
        return MessageCreator(context).apply(fn)
    }

    operator fun <T> invoke(fn: CommandMessage.() -> T): T {
        return fn(this)
    }

    fun checkOperatorCommand(){
        if (context.messageType != MessageType.PRIVATE || context.describeSender() !in config.operator) {
            throw CommandIgnoredException()
        }
    }

    companion object {
        //wtf heybox
        val regex = Pattern.compile("[\\s ]")
    }
}