package icu.takeneko.nekobot

import icu.takeneko.nekobot.command.CommandManager
import icu.takeneko.nekobot.command.management.GroupCommand
import icu.takeneko.nekobot.command.management.GroupRuleCommand
import icu.takeneko.nekobot.command.management.HelpCommand
import icu.takeneko.nekobot.command.minecraft.MinecraftVersionCommand
import icu.takeneko.nekobot.command.minecraft.VersionCacheCommand
import icu.takeneko.nekobot.command.minecraft.YarnClassCommand
import icu.takeneko.nekobot.command.minecraft.YarnFieldCommand
import icu.takeneko.nekobot.command.minecraft.YarnMethodCommand
import icu.takeneko.nekobot.command.minecraft.mappingRepository
import icu.takeneko.nekobot.command.minecraft.versionRepository
import icu.takeneko.nekobot.command.status.PingCommand
import icu.takeneko.nekobot.command.utility.CalculatorCommand
import icu.takeneko.nekobot.command.utility.PreferenceCommand
import icu.takeneko.nekobot.config.loadConfig
import icu.takeneko.nekobot.i18n.I18n
import icu.takeneko.nekobot.mcversion.MinecraftVersion
import icu.takeneko.nekobot.message.CommandContext
import icu.takeneko.nekobot.message.MessageResponseCreationScope
import icu.takeneko.nekobot.preference.Preference
import icu.takeneko.nekobot.util.getVersionInfoString
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class NekoBot(private val commandPrefix: String = "!") {
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(2)
    private val logger = LoggerFactory.getLogger("NekoBot")
    val commandManager: CommandManager = CommandManager(commandPrefix)

    private fun bootstrapMinecraftServices() {
        logger.info("Updating Mapping version.")
        mappingRepository.updateVersion()
        logger.info("Updating Minecraft version.")
        MinecraftVersion.update()
        logger.info("Updating Minecraft version for mapping.")
        if (!versionRepository.update()) {
            throw IllegalStateException("Update Minecraft version for mapping failed.")
        }
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
    }

    private fun bootstrapCommands() {
        logger.info("Registering Command.")
        commandManager.apply {
            register(PingCommand())
            register(YarnClassCommand())
            register(YarnMethodCommand())
            register(YarnFieldCommand())
            register(HelpCommand())
            if (Environment.permissionManagementEnabled) {
                register(GroupCommand())
                register(GroupRuleCommand())
            }
            register(VersionCacheCommand())
            register(MinecraftVersionCommand())
            register(CalculatorCommand())
            register(PreferenceCommand())
        }
        logger.info("Command Registered: ${commandManager.commandPrefixes.joinToString(", ")}")
    }

    fun preBootstrap() {
        logger.info("Nya!")
        logger.info("Loading config.")
        loadConfig()
    }

    fun bootstrap() {
        if (!Preference.load()) {
            logger.error("Failed to initalize Preference subsystem.")
            logger.error("Refused to start application to prevent data loss.")
            throw IllegalStateException("")
        }
        try {
            I18n.init()
        } catch (e: Throwable) {
            logger.error("Failed to initialize translation subsystem.", e)
            throw RuntimeException(e)
        }
        bootstrapMinecraftServices()
        bootstrapCommands()
        logger.info("Bootstrapped NekoBot " + getVersionInfoString())
    }

    fun acceptCommand(ctx: CommandContext): MessageResponseCreationScope? {
        return commandManager.run(ctx)
    }
}