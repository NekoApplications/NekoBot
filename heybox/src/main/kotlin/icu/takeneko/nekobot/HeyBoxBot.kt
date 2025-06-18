package icu.takeneko.nekobot

import icu.takeneko.nekobot.config.config
import org.slf4j.LoggerFactory
import java.util.concurrent.locks.LockSupport
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("NekoBot/HeyBox")

fun main() {
    val timeStart = System.currentTimeMillis()
    val bot = NekoBot()
    bot.preBootstrap()
    if (config.token.isEmpty()) {
        logger.error("HeyBox token was not properly configured.")
        logger.error("Looks like NekoBot is not properly configured at current directory, application will not start up until the errors are resolved.")
        exitProcess(1)
    }
    logger.info("Bootstrapping NekoBot.")
    bot.bootstrap()
    logger.info("Starting HeyBox bot using token: " + config.token)
    val timeComplete = System.currentTimeMillis()
    val timeUsed = (java.lang.Long.valueOf(timeComplete - timeStart).toString() + ".0f").toFloat() / 1000
    logger.info("Done(${timeUsed}s)!.")

    while (true) {
        LockSupport.parkNanos(100)
    }
}