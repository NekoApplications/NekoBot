package icu.takeneko.nekobot.heybox.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

object HeyboxApi {
    var token: String = ""
    val logger = LoggerFactory.getLogger("HeyboxApi")

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun updateMessage(
        newContent: String,
        messageId: String,
        channelId: String,
        roomId: String
    ): CommonResult? {
        val (_, result) = post<UpdateMessage, CommonResult>(
            "/v2/channel_msg/update",
            UpdateMessage(
                roomId,
                newContent,
                channelId,
                messageId
            )
        )
        return result
    }

    suspend fun addReaction(
        emoji: String,
        messageId: String,
        channelId: String,
        roomId: String
    ): CommonResult? {
        val (_, result) = post<ReactionRequest, CommonResult>(
            "/v2/channel_msg/emoji/reply",
            ReactionRequest(
                messageId,
                emoji,
                1,
                channelId,
                roomId
            )
        )
        return result
    }

    suspend fun sendMessage(
        message: String,
        channelId: String,
        roomId: String,
        replyId: String = ""
    ): MessageRequestResult? {
        val (_, result) = post<OutcomingMessage, MessageRequestResult>(
            "/v2/channel_msg/send",
            OutcomingMessage(
                roomId,
                message,
                channelId,
                replyId = replyId
            )
        )
        return result
    }

    suspend inline fun <reified T, reified R> post(endpoint: String, body: T): Pair<HttpStatusCode, R?> {
        val resp = postRaw(endpoint, body)
        return resp.status to resp.body<R?>()
    }

    suspend inline fun <reified T, reified R> postForString(endpoint: String, body: T): Pair<HttpStatusCode, String> {
        val resp = postRaw<T>(endpoint, body)
        return resp.status to resp.bodyAsText()
    }

    suspend inline fun <reified T> postRaw(endpoint: String, body: T): HttpResponse {
        val url = "https://chat.xiaoheihe.cn/chatroom$endpoint"
        val resp = client.post(url) {
            parameter("chat_os_type", "bot")
            contentType(ContentType.Application.Json)
            header("token", token)
            header("client_type", "heybox_chat")
            header("x_client_type", "web")
            header("os_type", "web")
            header("x_os_type", "bot")
            header("x_app", "heybox_chat")
            header("chat_version", "1.30.0")
            setBody(body)
            timeout {
                this.requestTimeoutMillis = 5000
            }
        }
        logger.debug("Request {} {}", url, resp)
        return resp
    }
}