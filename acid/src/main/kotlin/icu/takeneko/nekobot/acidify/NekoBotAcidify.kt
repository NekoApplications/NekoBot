package icu.takeneko.nekobot.acidify

import icu.takeneko.nekobot.NekoBot
import icu.takeneko.nekobot.acidify.util.asMessageType
import icu.takeneko.nekobot.acidify.util.generateTerminalQRCode
import icu.takeneko.nekobot.util.getVersionInfoString
import io.ktor.util.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.ntqqrev.acidify.Bot
import org.ntqqrev.acidify.common.AppInfo
import org.ntqqrev.acidify.common.SessionStore
import org.ntqqrev.acidify.common.UrlSignProvider
import org.ntqqrev.acidify.event.AcidifyEvent
import org.ntqqrev.acidify.event.BotOfflineEvent
import org.ntqqrev.acidify.event.MessageReceiveEvent
import org.ntqqrev.acidify.event.QRCodeGeneratedEvent
import org.ntqqrev.acidify.logging.LogHandler
import org.ntqqrev.acidify.logging.LogLevel
import org.ntqqrev.acidify.login
import org.ntqqrev.acidify.message.BotIncomingMessage
import org.ntqqrev.acidify.message.MessageScene
import org.ntqqrev.acidify.qrCodeLogin
import org.ntqqrev.acidify.sendFriendMessage
import org.ntqqrev.acidify.sendGroupMessage
import org.slf4j.LoggerFactory
import java.util.concurrent.locks.LockSupport
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

object NekoBotAcidify : CoroutineScope {
    private val dispatcher = Dispatchers.IO.limitedParallelism(Runtime.getRuntime().availableProcessors())
    private val logger = LoggerFactory.getLogger("NekoBot-Acidify")
    private lateinit var config: AcidifyConfig
    private lateinit var signProvider: UrlSignProvider
    private lateinit var sessionStore: SessionStore
    private lateinit var appInfo: AppInfo
    private lateinit var nekoBot: NekoBot
    private lateinit var bot: Bot
    private var shouldKeepRunning = true

    @JvmStatic
    fun main(args: Array<String>) {
        logger.info("Launching NekoBot ${getVersionInfoString()}")
        this.config = AcidifyConfig.getOrCreateConfig() ?: return
        this.signProvider = UrlSignProvider(config.signUrl, getProxyUrl())
        if (config.signUrl.isEmpty()) {
            logger.error("No sign url configured.")
            logger.error("Looks like NekoBot Acidify is not properly configured, NekoBot will not start until all the errors are resolved.")
            exitProcess(1)
        }
        logger.info("Using sign server: ${config.signUrl}")
        this.appInfo = runBlocking {
            logger.info("Fetching AppInfo from sign server.")
            signProvider.getAppInfo()
        } ?: AcidifyConfig.getOrCreateAppInfo() ?: return
        logger.info("Using AppInfo: $appInfo")
        if (config.persistentSessionStorage) {
            logger.info("Reading SessionStorage from local file.")
            this.sessionStore = SessionStorageSupport.getOrCreateSessionStorage() ?: return
        } else {
            this.sessionStore = SessionStore.empty()
        }
        this.nekoBot = NekoBot()
        nekoBot.preBootstrap()
        nekoBot.bootstrap()
        logger.info("Logging in using protocol ${appInfo.os} ${appInfo.currentVersion} (AppId: ${appInfo.subAppId})")
        this.bot = runBlocking {
            Bot.create(
                appInfo = appInfo,
                sessionStore = sessionStore,
                signProvider = signProvider,
                scope = this,
                minLogLevel = LogLevel.DEBUG,
                logHandler = logger.toAcidifyLogHandler()
            )
        }
        if (config.persistentSessionStorage) {
            SessionStorageSupport.configureSessionAutoSave(bot)
        }
        bot.subscribe<QRCodeGeneratedEvent> {
            logger.info("Scan the QrCode with Mobile QQ to login. \n${generateTerminalQRCode(this.url)}")
        }
        val job = this.launch {
            if (config.persistentSessionStorage) {
                bot.login()
            } else {
                bot.qrCodeLogin()
            }
        }
        runBlocking {
            job.join()
        }
        configureEventListeners()
        while (shouldKeepRunning) {
            LockSupport.parkNanos(1000)
        }
    }

    fun configureEventListeners() {
        bot.subscribe<BotOfflineEvent> {
            logger.warn("NekoBot has went offline, reason: ${this.reason}")
            shouldKeepRunning = false
        }
        bot.subscribe<MessageReceiveEvent> {
            val incoming = this.message
            val plain = incoming.segments.joinToString { it.toString() }
            val message = AcidifyMessage(
                incoming.senderUin.toString(),
                incoming.peerUin.toString(),
                incoming.scene.asMessageType() ?: return@subscribe,
                this@NekoBotAcidify.nekoBot.commandManager,
                plain
            )
            val desc = if (incoming.extraInfo == null) {
                "[${incoming.senderUin}]"
            } else {
                "[${incoming.extraInfo!!.groupCard}(${incoming.senderUin}) | ${incoming.extraInfo!!.specialTitle}]"
            }
            val scene = if (incoming.scene == MessageScene.FRIEND) {
                "[${incoming.scene}" + if (incoming.extraInfo != null) {
                    " |" + incoming.extraInfo!!.nick
                } else {
                    ""
                } + "]"
            } else {
                "[${incoming.scene} | ${incoming.peerUin}]"
            }
            logger.info("[${incoming.messageUid}] $scene $desc -> $plain")
            val response = nekoBot.acceptCommand(message) ?: return@subscribe
            val out = response.builder.joinToString("\n")
            logger.info("[${incoming.messageUid}] <- $out")
            launch {
                incoming.sendResponse(out)
            }
        }
    }

    private suspend fun BotIncomingMessage.sendResponse(plain: String) {
        when (this.scene) {
            MessageScene.FRIEND -> {
                bot.sendFriendMessage(
                    this.senderUin
                ) {
                    this.text(plain)
                }
            }

            MessageScene.GROUP -> {
                bot.sendGroupMessage(
                    this.peerUin
                ) {
                    this.reply(this@sendResponse.sequence)
                    this.text(plain)
                }
            }

            else -> {}
        }
    }

    private fun getProxyUrl(): String? {
        val host: String? = System.getProperty("http.proxyHost") ?: return null
        val port: String? = System.getProperty("http.proxyPort") ?: return null

        return "http://$host:$port"
    }

    inline fun <reified T : AcidifyEvent> Bot.subscribe(noinline handler: suspend T.() -> Unit) {
        launch {
            this@subscribe.eventFlow.filterIsInstance<T>().collect(handler)
        }
    }

    private fun Logger.toAcidifyLogHandler(): LogHandler =
        LogHandler { level, tag, message, throwable ->
            when (level) {
                LogLevel.VERBOSE -> trace(message)
                LogLevel.DEBUG -> debug(message)
                LogLevel.INFO -> info(message)
                LogLevel.WARN -> if (throwable == null) {
                    warn(message)
                } else {
                    warn(message, throwable)
                }

                LogLevel.ERROR -> if (throwable == null) {
                    error(message)
                } else {
                    error(message, throwable)
                }
            }
        }

    override val coroutineContext: CoroutineContext
        get() = dispatcher
}