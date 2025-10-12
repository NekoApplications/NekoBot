package icu.takeneko.nekobot.pe

import icu.takeneko.nekobot.NekoBot
import icu.takeneko.nekobot.message.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

class NekoBotInstance(val prefix: String, private val callbackHandle: CallbackHandle) : CoroutineScope {
    private val dispatcher = Dispatchers.IO.limitedParallelism(Runtime.getRuntime().availableProcessors())
    override val coroutineContext: CoroutineContext
        get() = dispatcher
    private val bot = NekoBot(prefix)
    private val logger = LoggerFactory.getLogger("NekoBotInstance")

    @OptIn(ObsoleteCoroutinesApi::class)
    private val messageChannel = actor<Message>(dispatcher) {
        for (m in this.channel) {
            bot.acceptCommand(m).run {
                callbackHandle.accept(this?.builder)
            }
        }
    }

    fun launchBot() {
        logger.info("Using prefix {}", prefix)
        bot.preBootstrap()
        bot.bootstrap()
    }

    fun handle(sender: String, channel: String, isPrivateMessage: Boolean, plainMessage: String) {
        launch {
            messageChannel.send(
                Message(
                    sender,
                    channel,
                    if (isPrivateMessage) MessageType.PRIVATE else MessageType.GROUP,
                    bot.commandManager,
                    plainMessage
                )
            )
        }

    }
}