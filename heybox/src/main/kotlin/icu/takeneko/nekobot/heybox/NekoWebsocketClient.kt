package icu.takeneko.nekobot.heybox

import com.alibaba.fastjson2.JSONObject
import icu.takeneko.nekobot.heybox.event.EventDispatcher
import net.cjsah.bot.HeartBeatTimer
import net.cjsah.bot.Main
import net.cjsah.bot.SignalType
import net.cjsah.bot.event.EventType
import net.cjsah.bot.event.events.CommandEvent
import net.cjsah.bot.util.JsonUtil
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.CloseFrame
import org.java_websocket.handshake.ServerHandshake
import org.quartz.SchedulerException
import org.slf4j.LoggerFactory
import java.net.URI

class NekoWebsocketClient(token: String) :
    WebSocketClient(URI("wss://chat.xiaoheihe.cn/chatroom/ws/connect?chat_os_type=bot")) {
    private val heart: HeartBeatTimer
    private val logger = LoggerFactory.getLogger("WebSocketClientImpl")

    init {
        this.addHeader("client_type", "heybox_chat")
        this.addHeader("x_client_type", "web")
        this.addHeader("os_type", "web")
        this.addHeader("x_os_type", "bot")
        this.addHeader("x_app", "heybox_chat")
        this.addHeader("chat_version", "1.24.5")
        this.addHeader("token", token)
        this.heart = HeartBeatTimer({
            if (this.isOpen) {
                this.send("PING")
            }
        }, { Main.sendSignal(SignalType.RE_CONNECT) })
    }

    override fun close() {
        super.close()
        this.heart.stop()
    }

    fun shutdown() {
        this.closeBlocking()
        this.heart.cancel()
    }

    override fun onOpen(handshake: ServerHandshake?) {
        logger.info("Connection established.")
        try {
            this.heart.start()
        } catch (e: SchedulerException) {
            logger.error("Unable to start Heartbeat service", e)
            Main.sendSignal(SignalType.RE_CONNECT)
        }
    }

    override fun onMessage(msg: String) {
        logger.debug("Incoming message: {}", msg)
        if ("PONG" == msg) {
            this.heart.heartPong()
            return
        }
        try {
            val json = JsonUtil.deserialize(msg)
            val type: String? = json.getString("type")
            val eventType = EventType.getByType(type)
            if (eventType == null) {
                logger.warn("Unknown event type: {}, {}", type, json)
                return
            }
            val data: JSONObject? = json.getJSONObject("data")
            val event = eventType.factory.apply(data)

            EventDispatcher.dispatch(event)
        } catch (e: Throwable) {
            logger.error("An exception was thrown from websocket service", e)
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        if (code == CloseFrame.NORMAL) return
        logger.warn("Disconnected from server, code: {}, reason: {}", code, reason)
    }

    override fun onError(e: Exception?) {
        logger.error("An exception was thrown from websocket service", e)
    }
}
