package icu.takeneko.nekobot

import icu.takeneko.nekobot.config.config
import icu.takeneko.nekobot.heybox.ws.NekoWebsocketClient
import icu.takeneko.nekobot.heybox.event.EventDispatcher
import icu.takeneko.nekobot.message.CommandContextHeyboxImpl
import icu.takeneko.nekobot.message.MessageResponseCreationScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
import net.cjsah.bot.api.Api
import net.cjsah.bot.api.CardBuilder
import net.cjsah.bot.api.card.CardItem
import net.cjsah.bot.data.TextType
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
val bot = NekoBot("/")

fun main() {
    HeyboxEnvironment.mainThread = Thread.currentThread()
    val timeStart = System.currentTimeMillis()
    CoreEnvironment.permissionManagementEnabled = false
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
    logger.info("Starting HeyBox bot using token: " + config.token)
    websocketClient = NekoWebsocketClient(config.token)
    val timeComplete = System.currentTimeMillis()
    val timeUsed = (timeComplete - timeStart) / 1000f
    Runtime.getRuntime().addShutdownHook(thread(start = false, name = "ShutdownThread") {
        HeyboxEnvironment.destroy()
        if (websocketClient != null) {
            websocketClient!!.shutdown()
        }
    })
    EventDispatcher.subscribe<CommandEvent> {
        val context = CommandContextHeyboxImpl(this, bot.commandManager)
        logger.info("[{}/{}] -> {}", this.senderInfo.nickname, this.senderInfo.id, context.messagePlain)
        val ret = bot.acceptCommand(context) ?: return@subscribe
        Api.sendCardMsg(
            CardBuilder(this.roomInfo.id, this.channelInfo.id)
                .replay(this.msgId)
                .card { item ->
                    createResponseCard(ret, item)
                }
        )
    }
    //look at my eyes, tell me why, why baby why
    EventDispatcher.subscribe<MessageEvent> {
        val context = CommandContextHeyboxImpl(this, bot.commandManager)
        logger.info("[{}/{}] -> {}", this.userName, this.userId, context.messagePlain)
        val ret = bot.acceptCommand(context) ?: return@subscribe
        Api.sendCardMsg(
            CardBuilder(this.roomId, this.channelId)
                .replay(this.msgId)
                .card { item ->
                    createResponseCard(ret, item)
                }
        )
    }
    logger.info("Done(${timeUsed}s)!.")
    websocketClient!!.connectBlocking()
    while (shouldKeepRunning) {
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

private fun createResponseCard(
    ret: MessageResponseCreationScope,
    item: CardItem
) {
    val stringBuilder = StringBuilder()
    for (string in ret.builder) {
        val content = string.replace("<", "＜").replace(">", "＞")
        if (string.replace(" ", "") == "\n") {
            item.section {
                it.text(TextType.MARKDOWN, stringBuilder.toString())
            }
            stringBuilder.clear()
            continue
        }
        stringBuilder.append(content)
    }
    if (stringBuilder.isNotEmpty()) {
        item.section {
            it.text(TextType.MARKDOWN, stringBuilder.toString())
        }
    }
}