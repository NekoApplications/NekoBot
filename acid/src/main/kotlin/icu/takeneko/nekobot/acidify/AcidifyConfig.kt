package icu.takeneko.nekobot.acidify

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.ntqqrev.acidify.common.AppInfo
import org.slf4j.LoggerFactory
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Serializable
data class AcidifyConfig(
    val persistentSessionStorage: Boolean = true,
    val signUrl: String = "",
) {
    companion object {
        val configPath = Path("./config.acidify.json")
        val appInfoPath = Path("./config.acidify.appinfo.json")

        private val json = Json {
            encodeDefaults = true
            prettyPrint = true
        }
        private val logger = LoggerFactory.getLogger("AcidifyConfig")

        fun getOrCreateConfig(): AcidifyConfig? {
            if (configPath.exists()) {
                return try {
                    json.decodeFromString<AcidifyConfig>(configPath.readText()).also {
                        configPath.deleteIfExists()
                        configPath.createFile()
                        configPath.writeText(json.encodeToString(it))
                    }
                } catch (e: Exception) {
                    logger.error(
                        "Looks like NekoBot Acidify is not properly configured, NekoBot will not start until all the errors are resolved.",
                        e
                    )
                    null
                }
            }
            configPath.deleteIfExists()
            configPath.createFile()
            configPath.writeText(json.encodeToString(AcidifyConfig()))
            logger.info("Creating default NekoBot Acidify config file.")
            return AcidifyConfig()
        }

        fun getOrCreateAppInfo(): AppInfo? {
            if (appInfoPath.exists()) {
                return try {
                    json.decodeFromString<AppInfo>(appInfoPath.readText()).also {
                        appInfoPath.deleteIfExists()
                        appInfoPath.createFile()
                        appInfoPath.writeText(json.encodeToString(it))
                    }
                } catch (e: Exception) {
                    logger.error(
                        "Looks like NekoBot Acidify is not properly configured, NekoBot will not start until all the errors are resolved.",
                        e
                    )
                    null
                }
            }
            appInfoPath.deleteIfExists()
            appInfoPath.createFile()
            appInfoPath.writeText(json.encodeToString(AppInfo.Bundled.Linux))
            logger.info("Creating default NekoBot Acidify AppInfo file.")
            return AppInfo.Bundled.Linux
        }
    }
}
