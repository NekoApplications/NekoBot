package icu.takeneko.nekobot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.handlers.ErrorHandler
import com.github.kotlintelegrambot.dispatcher.handlers.TextHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.types.TelegramBotResult
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.Proxy

class TelegramBotInstance(
    private val nekoBot: NekoBot
) {

    private var token = ""
    private lateinit var botInstance: Bot
    private val logger = LoggerFactory.getLogger("TelegramBot")
    fun useToken(token: String) {
        this.token = token
    }

    fun create() {
        val systemProxy = getProxy()
        if (systemProxy != Proxy.NO_PROXY) {
            logger.info("Using proxy: $systemProxy")
        }
        botInstance = bot {
            this@bot.token = this@TelegramBotInstance.token
            dispatch(::configureDispatch)

            proxy = systemProxy
        }
    }

    fun getProxy(): Proxy {
        val proxyHost = System.getProperty("http.proxyHost", "")
        val proxyPort = System.getProperty("http.proxyPort", "").toIntOrNull() ?: return Proxy.NO_PROXY
        if (proxyHost.isNotEmpty()) {
            return Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(proxyHost, proxyPort))
        }
        return Proxy.NO_PROXY
    }

    fun configureDispatch(dispatcher: Dispatcher) {
        dispatcher.text(handleText = ::handleTextMessage)
        dispatcher.addErrorHandler(ErrorHandler {
            logger.error("Bot ${this.bot} encountered an error: ${this.error.getErrorMessage()}")
        })
    }

    fun runBlocking() {
        botInstance.startPolling()
    }

    fun handleTextMessage(env: TextHandlerEnvironment) {
        nekoBot.launch {
            if (env.message.chat.type == "private" || env.message.chat.type.contains("group")) {
                logger.info(
                    "[${env.message.from?.username ?: "UNKNOWN"}]${
                        if (env.message.chat.type.contains("group")) "(${env.message.chat.title})" else ""
                    } -> ${env.text}"
                )
                val ctx = CommandContextTelegramImpl(env, nekoBot.commandManager)
                val result = nekoBot.acceptCommand(ctx) ?: return@launch
                val string = TelegramMarkdownSanitizer.accept(result.builder.joinToString(separator = ""))
                logger.info("$string -> [${env.message.from?.username ?: "UNKNOWN"}]")
                val sendResult = botInstance.sendMessage(
                    chatId = ChatId.fromId(env.message.chat.id),
                    messageThreadId = env.message.messageThreadId,
                    text = string,
                    parseMode = ParseMode.MARKDOWN_V2,
                    replyToMessageId = env.message.messageId
                )
                sendResult.onError {
                    when (it) {
                        is TelegramBotResult.Error.HttpError -> {
                            logger.error("Http Error <- ${it.httpCode} ${it.description}")
                        }

                        is TelegramBotResult.Error.InvalidResponse -> {
                            logger.error("Invalid Response <- $it")
                        }

                        is TelegramBotResult.Error.TelegramApi -> {
                            logger.error("API Error <- ${it.errorCode} ${it.description}")
                        }

                        is TelegramBotResult.Error.Unknown -> {
                            logger.error("Unknown Error <- ${it.exception}")
                        }
                    }
                }
            }
        }
    }
}