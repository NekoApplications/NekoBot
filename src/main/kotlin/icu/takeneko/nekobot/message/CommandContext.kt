package icu.takeneko.nekobot.message

import icu.takeneko.nekobot.Context
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder


data class CommandContext(
    val platformMessage: MessageChain,
    val group: Group?,
    val source: User,
    val messageType: MessageType
) :Context{
    val messagePlain: String
        get() = platformMessage.contentToString()

    fun describeSender() = source.id.toString()

    fun describeGroup() = group!!.id.toString()

    fun isGroupMessage() = messageType == MessageType.GROUP

    fun isPrivateMessage() = messageType == MessageType.PRIVATE

    override fun descriptor(): String {
        return describeSender()
    }
}

enum class MessageType {
    GROUP, PRIVATE
}

class MessageResponseCreationScope(val context: CommandContext) {
    private val builder = MessageChainBuilder()

    constructor(
        context: CommandContext,
        fn: MessageResponseCreationScope.() -> Unit
    ) : this(context) {
        this.fn()
    }

    operator fun String.unaryPlus() {
        builder.add(this + "\n")
    }

    fun append(string: String) {
        builder.add(string)
    }

    fun create(): MessageChain? {
        if (builder.isEmpty())return null
        return builder.asMessageChain()
    }

    operator fun invoke(fn: MessageResponseCreationScope.() -> Unit): MessageResponseCreationScope {
        fn(this)
        return this
    }

}