package icu.takeneko.nekobot

import icu.takeneko.nekobot.message.MessageResponseCreationScope
import icu.takeneko.nekobot.message.MessageType
import icu.takeneko.nekobot.message.CommandContextMiraiImpl
import icu.takeneko.nekobot.util.BuildProperties
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import top.mrxiaom.overflow.contact.RemoteBot
import java.lang.management.ManagementFactory
import java.util.concurrent.*
import kotlin.system.exitProcess

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "icu.takeneko.nekobot",
        name = "NekoBot",
        version = BuildProperties["coreVersion"]!!
    ) {
        author("ZhuRuoLing")
    }
) {

    private val botInstance = NekoBot()

    override fun onEnable() {
        val future = CompletableFuture.runAsync {
            botInstance.preBootstrap()
            botInstance.bootstrap()
            val eventChannel = GlobalEventChannel.parentScope(this)

            subscribe<GroupMessageEvent> {
                val ret = botInstance.acceptCommand(
                    CommandContextMiraiImpl(
                        message,
                        this.group,
                        this.sender,
                        MessageType.GROUP,
                        botInstance.commandManager
                    ),
                ) ?: return@subscribe
                group.sendMessage(ret.mirai())
            }

            subscribe<FriendMessageEvent> {
                val ret = botInstance.acceptCommand(
                    CommandContextMiraiImpl(
                        message,
                        null,
                        this@subscribe.sender,
                        MessageType.PRIVATE,
                        botInstance.commandManager
                    )
                ) ?: return@subscribe
                sender.sendMessage(ret.mirai())
            }

            subscribe<BotOnlineEvent> {
                if (this.bot !is RemoteBot) return@subscribe
                val remoteBot  = this.bot as RemoteBot
                CoreEnvironment.implementingPlatformSuffix = "[${remoteBot.appName}]"
            }
        }

        try {
            future.get(30, TimeUnit.SECONDS)
        } catch (_: TimeoutException) {
            logger.error("Plugin initialization does not complete within 30 seconds, dumping threads.")
            val bean = ManagementFactory.getThreadMXBean()
            bean.dumpAllThreads(true, true).forEach {
                logger.error(it.toString())
            }
            logger.error("Cowardly refusing to start application because a broken application state.")
            exitProcess(1)
        } catch (e: ExecutionException) {
            logger.error("Plugin initialization failed to complete.", e)
            logger.error("Cowardly refusing to start application because a broken application state.")
            exitProcess(1)
        }
    }

    inline fun <reified T : Event> subscribe(noinline handler: suspend T.() -> Unit) {
        GlobalEventChannel.parentScope(this).subscribeAlways<T> {
            launch {
                this@subscribeAlways.handler()
            }
        }
    }

    fun MessageResponseCreationScope.mirai(): MessageChain {
        return MessageChainBuilder().apply {
            for (string in this@mirai.builder) {
                this.append(string)
            }
        }.build()
    }
}
