package icu.takeneko.nekobot.message

import kotlinx.serialization.Serializable

@Serializable
data class Message(val scene: String, val messageType: MessageType, val messagePlain: String, val status:Boolean = true, val forward:Boolean = false)

enum class MessageType {
    GROUP, PRIVATE
}

class MessageResponse(val scene: String, val messageType: MessageType) {
    val messageText = StringBuilder()

    operator fun String.unaryPlus(){
        messageText.append(this + "\n")
    }

    fun append(string: String){
        messageText.append(string)
    }
    fun toMessage(status: Boolean = true, forward: Boolean = false) = Message(scene, messageType, messageText.toString(), status, forward)
}

fun MessageResponse(scene: String, messageType: MessageType, fn: MessageResponse.() -> Unit):MessageResponse {
    return MessageResponse(scene, messageType).apply(fn)
}