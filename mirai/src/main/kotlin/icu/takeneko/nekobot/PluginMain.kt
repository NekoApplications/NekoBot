package icu.takeneko.nekobot

import icu.takeneko.nekobot.message.MessageResponseCreationScope
import icu.takeneko.nekobot.message.MessageType
import icu.takeneko.nekobot.message.CommandContextMiraiImpl
import icu.takeneko.nekobot.util.BuildProperties
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
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
            botInstance.bootstrap()
            val eventChannel = GlobalEventChannel.parentScope(this)
            eventChannel.subscribeAlways<GroupMessageEvent> {
                launch {
                    val ret = botInstance.acceptCommand(
                        CommandContextMiraiImpl(
                            message,
                            this@subscribeAlways.group,
                            this@subscribeAlways.sender,
                            MessageType.GROUP,
                            botInstance.commandManager
                        ),
                    ) ?: return@launch
                    it.group.sendMessage(ret.mirai())
                }

            }
            eventChannel.subscribeAlways<FriendMessageEvent> {
                launch {
                    val ret = botInstance.acceptCommand(
                        CommandContextMiraiImpl(
                            message,
                            null,
                            this@subscribeAlways.sender,
                            MessageType.PRIVATE,
                            botInstance.commandManager
                        )
                    ) ?: return@launch
                    it.sender.sendMessage(ret.mirai())
                }
            }
            Unit
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

    fun MessageResponseCreationScope.mirai(): MessageChain {
        return MessageChainBuilder().apply {
            for (string in this@mirai.builder) {
                this.append(string)
            }
        }.build()
    }
}
