package icu.takeneko.nekobot

import icu.takeneko.nekobot.config.config
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("NekoBot/Telegram")
val bot = NekoBot("/")
val instance = TelegramBotInstance(bot)

fun main() {
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
    val timeComplete = System.currentTimeMillis()
    val timeUsed = (timeComplete - timeStart) / 1000f
    logger.info("Done(${timeUsed}s)!.")
    logger.info("Starting Telegram bot using token: " + config.token)
    instance.useToken(config.token)
    instance.create()
    instance.runBlocking()
}