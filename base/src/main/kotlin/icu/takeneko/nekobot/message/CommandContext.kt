package icu.takeneko.nekobot.message

import icu.takeneko.nekobot.command.CommandManager

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

enum class MessageType {
    GROUP, PRIVATE
}