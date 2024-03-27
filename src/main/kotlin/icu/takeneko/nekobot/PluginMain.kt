package icu.takeneko.nekobot

import icu.takeneko.nekobot.command.CommandManager
import icu.takeneko.nekobot.command.management.GroupCommand
import icu.takeneko.nekobot.command.management.GroupRuleCommand
import icu.takeneko.nekobot.command.management.HelpCommand
import icu.takeneko.nekobot.command.minecraft.*
import icu.takeneko.nekobot.command.status.StatusCommand
import icu.takeneko.nekobot.command.utility.CalculatorCommand
import icu.takeneko.nekobot.config.loadConfig
import icu.takeneko.nekobot.mcversion.MinecraftVersion
import icu.takeneko.nekobot.message.Message
import icu.takeneko.nekobot.message.MessageType
import icu.takeneko.nekobot.util.BuildProperties
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

val executor = Executors.newFixedThreadPool(4)
val scheduler = Executors.newScheduledThreadPool(2)

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "icu.takeneko.nekobot",
        name = "NekoBot",
        version = BuildProperties["version"]!!
    ) {
        author("ZhuRuoLing")
    }
) {

    override fun onEnable() {
        logger.info("Nya!")
        logger.info("Loading config.")
        loadConfig()
        logger.info("Updating Mapping version.")
        mappingRepository.updateVersion()
        logger.info("Updating Minecraft version.")
        MinecraftVersion.update()
        versionRepository.update()
        scheduler.scheduleWithFixedDelay(
            {
                mappingRepository.updateVersion()
                versionRepository.update()
                MinecraftVersion.update()
            },
            0,
            2,
            TimeUnit.MINUTES
        )
        logger.info("Registering Command.")
        CommandManager.apply {
            register(StatusCommand())
            register(YarnClassCommand())
            register(YarnMethodCommand())
            register(YarnFieldCommand())
            register(HelpCommand())
            register(GroupCommand())
            register(GroupRuleCommand())
            register(VersionCacheCommand())
            register(MinecraftVersionCommand())
            register(CalculatorCommand())
        }
        logger.info("Command Registered: ${CommandManager.commandPrefixes.joinToString(", ")}")
        val eventChannel = GlobalEventChannel.parentScope(this)

        eventChannel.subscribeAlways<GroupMessageEvent> {
            launch {
                val ret = CommandManager.run(Message(message, this@subscribeAlways.group, this@subscribeAlways.sender, MessageType.GROUP)) ?: return@launch
                ret.source.group!!.sendMessage(ret.asMessageChain())
            }

        }
        eventChannel.subscribeAlways<FriendMessageEvent> {
            launch {
                val ret = CommandManager.run(Message(message, null, this@subscribeAlways.sender, MessageType.PRIVATE))
                    ?: return@launch
                ret.source.source.sendMessage(ret.asMessageChain())
            }
        }
    }

    override fun onDisable() {
        super.onDisable()

    }
}
