package icu.takeneko.nekobot.acidify

import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.ntqqrev.acidify.Bot
import org.ntqqrev.acidify.common.SessionStore
import org.ntqqrev.acidify.event.SessionStoreUpdatedEvent
import org.slf4j.LoggerFactory
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object SessionStorageSupport {
    val sessionPath = Path("./config.acidify.session.json")
    private val json = Json {
        encodeDefaults = true
        prettyPrint = true
    }
    private val logger = LoggerFactory.getLogger("MessageSignSupport")

    fun getOrCreateSessionStorage(): SessionStore? {
        if (sessionPath.exists()) {
            return try {
                json.decodeFromString<SessionStore>(sessionPath.readText()).also {
                    sessionPath.deleteIfExists()
                    sessionPath.createFile()
                    sessionPath.writeText(json.encodeToString(it))
                }
            } catch (e: Exception) {
                logger.error(
                    "Looks like NekoBot Acidify is not properly configured, NekoBot will not start until all the errors are resolved.",
                    e
                )
                null
            }
        }
        sessionPath.deleteIfExists()
        sessionPath.createFile()
        sessionPath.writeText(json.encodeToString(SessionStore.empty()))
        logger.info("Creating default NekoBot Acidify AppInfo file.")
        return SessionStore.empty()
    }

    fun configureSessionAutoSave(bot: Bot) = NekoBotAcidify.launch {
        bot.eventFlow.filterIsInstance<SessionStoreUpdatedEvent>().collect {
            sessionPath.deleteIfExists()
            sessionPath.createFile()
            sessionPath.writeText(json.encodeToString(it.sessionStore))
        }
    }
}