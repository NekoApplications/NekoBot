package icu.takeneko.nekobot

import icu.takeneko.nekobot.config.config
import icu.takeneko.nekobot.heybox.NekoWebsocketClient
import icu.takeneko.nekobot.heybox.event.EventDispatcher
import icu.takeneko.nekobot.heybox.heybox
import icu.takeneko.nekobot.message.CommandContextHeyboxImpl
import icu.takeneko.nekobot.message.builder.LocalImageElement
import icu.takeneko.nekobot.message.builder.RemoteImageElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
import net.cjsah.bot.api.Api
import net.cjsah.bot.event.events.CommandEvent
import net.cjsah.bot.event.events.MessageEvent
import org.java_websocket.enums.ReadyState
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.LockSupport
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("NekoBot/HeyBox")
private var websocketClient: NekoWebsocketClient? = null
val coroutineScope = MainScope() + Dispatchers.IO.limitedParallelism(Runtime.getRuntime().availableProcessors())
val bot = NekoBot("/")

class HeyBoxBotHost(private val apiKey: String) : NekoBotHost {
    override fun uploadImage(image: LocalImageElement): RemoteImageElement {
        TODO("Not yet implemented")
    }
}

fun main() {
    val timeStart = System.currentTimeMillis()
    Environment.permissionManagementEnabled = false
    bot.preBootstrap()
    if (config.token.isEmpty()) {
        logger.error("HeyBox token was not properly configured.")
        logger.error("Looks like NekoBot is not properly configured at current directory, application will not start up until the errors are resolved.")
        exitProcess(1)
    }
    logger.info("Bootstrapping NekoBot.")
    bot.bootstrap()
    logger.info("Commands:")
    bot.commandManager.commands.forEach { t, u ->
        logger.info("    - " + u.helpMessage)
    }
    Api.setToken(config.token)
    val host = HeyBoxBotHost(config.token)
    logger.info("Starting HeyBox bot using token: " + config.token)
    websocketClient = NekoWebsocketClient(config.token)
    val timeComplete = System.currentTimeMillis()
    val timeUsed = (timeComplete - timeStart) / 1000f
    Runtime.getRuntime().addShutdownHook(thread(start = false, name = "ShutdownThread") {
        if (websocketClient != null) {
            websocketClient!!.shutdown()
        }
        coroutineScope.cancel()
    })
    EventDispatcher.subscribe<CommandEvent> {
        val context = CommandContextHeyboxImpl(this, bot.commandManager)
        logger.info("[{}/{}] -> {}", this.senderInfo.nickname, this.senderInfo.id, context.messagePlain)
        val ret = bot.acceptCommand(context) ?: return@subscribe
        Api.sendCardMsg(
            ret.heybox(this.roomInfo.id, this.channelInfo.id, host)
                .replay(this.msgId)
        )
    }
    //look at my eyes, tell me why, why baby why
    EventDispatcher.subscribe<MessageEvent> {
        val context = CommandContextHeyboxImpl(this, bot.commandManager)
        logger.info("[{}/{}] -> {}", this.userName, this.userId, context.messagePlain)
        val ret = bot.acceptCommand(context) ?: return@subscribe
        Api.sendCardMsg(
            ret.heybox(this.roomId, this.channelId, host)
                .replay(this.msgId)
        )
    }
    logger.info("Done(${timeUsed}s)!.")
    websocketClient!!.connectBlocking()
    startWebsocketStateWatcher()
    while (true) {
        LockSupport.parkNanos(100)
    }
}


fun startWebsocketStateWatcher() {
    thread(start = true, name = "WebsocketStateWatcher", isDaemon = true) {
        while (true) {
            if (websocketClient != null) {
                if (websocketClient!!.readyState == ReadyState.CLOSED) {
                    logger.warn("Disconnected from HeyBox Server.")
                    logger.info("Reconnecting.")
                    websocketClient!!.reconnectBlocking()
                }
            }
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1))
        }
    }
}