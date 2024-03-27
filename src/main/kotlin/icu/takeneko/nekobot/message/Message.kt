package icu.takeneko.nekobot.message

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder


data class Message(
    val platformMessage: MessageChain,
    val group: Group?,
    val source: User,
    val messageType: MessageType
) {
    val messagePlain: String
        get() = platformMessage.contentToString()

    fun describeSender() = source.id.toString()
}

enum class MessageType {
    GROUP, PRIVATE
}

class MessageResponse(val source: Message) {
    private val builder = MessageChainBuilder()

    operator fun String.unaryPlus() {
        builder.add(this + "\n")
    }

    fun append(string: String) {
        builder.add(string)
    }

    fun asMessageChain() = builder.asMessageChain()

    operator fun invoke(fn: MessageResponse.() -> Unit): MessageResponse {
        fn(this)
        return this
    }
}

fun MessageResponse(src: Message, fn: MessageResponse.() -> Unit): MessageResponse {
    return MessageResponse(src).apply(fn)
}