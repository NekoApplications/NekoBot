package icu.takeneko.nekobot.heybox

import cn.hutool.http.HttpRequest
import cn.hutool.http.Method
import com.alibaba.fastjson2.JSONObject
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import net.cjsah.bot.api.Api
import net.cjsah.bot.api.MsgBuilder
import net.cjsah.bot.exception.BuiltExceptions
import net.cjsah.bot.util.JsonUtil
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.util.function.Consumer

object HeyboxApi {
    var token: String = ""
    private val logger = LoggerFactory.getLogger("HeyboxApi")
    private val client = HttpClient(CIO) {
        install(ContentNegotiation){
            json(Json {
                encodeDefaults = true
            })
        }
    }

    fun sendMsg(builder: MsgBuilder): String {
        logger.info("[{}] [{}] <== {}", builder.roomId, builder.channelId, builder.msg)
        val res = postJson("https://chat.xiaoheihe.cn/chatroom/v2/channel_msg/send") { json: JSONObject ->
            json.put("channel_type", 1)
            json.put("msg_type", 10)
            json.put("room_id", builder.roomId)
            json.put("channel_id", builder.channelId)
            json.put("msg", builder.msg)
            json.put("reply_id", builder.replay)
            json.put("at_user_id", builder.atUsers)
            json.put("at_role_id", builder.atRoles)
            json.put("mention_channel_id", builder.atChannels)
            json.put("heychat_ack_id", builder.uuid)
            json.put("addition", "{}")
        }
        return res.getJSONObject("result").getString("msg_id")
    }

    private fun postJson(url: String?, consumer: Consumer<JSONObject>): JSONObject {
        val request = genRequest(url, Method.POST)
        val body = JSONObject()
        consumer.accept(body)
        request.body(JsonUtil.serialize(body))
        return request(request)
    }

    private fun genRequest(url: String?, method: Method?): HttpRequest {
        return HttpRequest.of(url + "?chat_os_type=bot")
            .method(method)
            .header("Content-Type", "application/json;charset=UTF-8;")
            .header("token", Api.TOKEN)
            .header("client_type", "heybox_chat")
            .header("x_client_type", "web")
            .header("os_type", "web")
            .header("x_os_type", "bot")
            .header("x_app", "heybox_chat")
            .header("chat_version", "1.24.5")
            .timeout(5000)
    }

    private fun request(request: HttpRequest): JSONObject {
        request.execute().use { response ->
            val bodyStr = String(response.bodyBytes(), StandardCharsets.UTF_8)
            val json = JsonUtil.deserialize(bodyStr)
            if ("ok" != json.getString("status")) {
                throw BuiltExceptions.REQUEST_FAILED.create(json.getString("msg"))
            }
            return json
        }
    }
}