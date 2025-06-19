package icu.takeneko.nekobot.message

import icu.takeneko.nekobot.command.CommandManager
import icu.takeneko.nekobot.message.Context

interface CommandContext : Context {
    val messagePlain: String
    val messageType: MessageType
    fun describeSender(): String

    fun describeGroup(): String

    fun isGroupMessage(): Boolean

    fun isPrivateMessage(): Boolean

    fun manager(): CommandManager

    override fun descriptor(): String {
        return describeSender()
    }
}

class MessageResponseCreationScope(val context: CommandContext) {
    val builder: MutableList<String> = mutableListOf()

    constructor(
        context: CommandContext,
        fn: MessageResponseCreationScope.() -> Unit
    ) : this(context) {
        this.fn()
    }

    operator fun String.unaryPlus() {
        builder.add("$this  \n")
    }

    fun append(string: String) {
        builder.add(string)
    }

    operator fun invoke(fn: MessageResponseCreationScope.() -> Unit): MessageResponseCreationScope {
        fn(this)
        return this
    }
}

enum class MessageType {
    GROUP, PRIVATE
}