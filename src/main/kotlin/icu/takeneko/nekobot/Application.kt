package icu.takeneko.nekobot

import io.ktor.server.application.*
import icu.takeneko.nekobot.command.CommandManager
import icu.takeneko.nekobot.command.management.HelpCommand
import icu.takeneko.nekobot.command.management.GroupCommand
import icu.takeneko.nekobot.command.management.GroupRuleCommand
import icu.takeneko.nekobot.command.minecraft.*
import icu.takeneko.nekobot.command.status.StatusCommand
import icu.takeneko.nekobot.command.utility.CalculatorCommand
import icu.takeneko.nekobot.config.loadConfig
import icu.takeneko.nekobot.mcversion.MinecraftVersion
import icu.takeneko.nekobot.plugins.*
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

val executor = Executors.newFixedThreadPool(4)
val scheduler = Executors.newScheduledThreadPool(2)
private val logger = LoggerFactory.getLogger("Main")
fun main(args: Array<String>) {
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
    logger.info("Launching Application Engine.")
    io.ktor.server.netty.EngineMain.main(args + "-port=9876")
}

fun Application.module() {
    configureMonitoring()
    configureSerialization()
    configureSockets()
    configureRouting()
}