package icu.takeneko.nekobot.heybox.api

import kotlinx.serialization.SerialName
import java.util.UUID

@kotlinx.serialization.Serializable
data class OutcomingMessage(
    @SerialName("room_id") val roomId: String,
    @SerialName("msg") val msg: String,
    @SerialName("channel_id") val channelId: String,
    @SerialName("msg_type") val msgType: Int = 4,
    @SerialName("heychat_ack_id") val heychatAckId: String = UUID.randomUUID().toString(),
    @SerialName("reply_id") val replyId: String = "",
    @SerialName("addition") val addition: String = "{}",
    @SerialName("at_user_id") val atUserId: String = "",
    @SerialName("at_role_id") val atRoleId: String = "",
    @SerialName("mention_channel_id") val mentionChannelId: String = "",
    @SerialName("channel_type") val channelType: Int = 1
)

@kotlinx.serialization.Serializable
data class UpdateMessage(
    @SerialName("room_id") val roomId: String,
    @SerialName("msg") val message: String,
    @SerialName("channel_id") val channelId: String,
    @SerialName("msg_id") val messageId: String,
    @SerialName("msg_type") val messageType: Int = 4,
    @SerialName("heychat_ack_id") val heychatAckId: String = UUID.randomUUID().toString(),
    @SerialName("addition") val addition: String = "{}",
    @SerialName("channel_type") val channelType: Int = 1
)

@kotlinx.serialization.Serializable
data class CommonResult(
    @SerialName("msg") val msg: String,
    @SerialName("status") val status: RequestStatus = RequestStatus.FAIL
)

@kotlinx.serialization.Serializable
data class ReactionRequest(
    @SerialName("msg_id") val messageId: String,
    @SerialName("emoji") val emoji: String,
    @SerialName("is_add") val action: Int,
    @SerialName("channel_id") val channelId: String,
    @SerialName("room_id") val roomId: String
)

@kotlinx.serialization.Serializable
data class MessageRequestResult(
    @SerialName("msg") val msg: String,
    @SerialName("result") val result: RequestResult? = null,
    @SerialName("status") val status: RequestStatus = RequestStatus.FAIL
)

@kotlinx.serialization.Serializable
enum class RequestStatus {
    @SerialName("ok")
    SUCCESS,
    @SerialName("failed")
    FAIL
}

@kotlinx.serialization.Serializable
data class RequestResult(
    @SerialName("chatmobile_ack_id") val chatmobileAckId: String,
    @SerialName("heychat_ack_id") val heychatAckId: String,
    @SerialName("msg_id") var messageId: String,
    @SerialName("msg_seq") var messageSeq: String
)