package icu.takeneko.nekobot.preference

import icu.takeneko.nekobot.Context
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.slf4j.LoggerFactory
import kotlin.io.path.*

object Preference {
    private val possibleKeys = listOf(
        "bot.i18n.language"
    )
    private val preferences: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
    private val preferencePath = Path("preferences.json")
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
    private val logger = LoggerFactory.getLogger("Preference")

    @OptIn(ExperimentalSerializationApi::class)
    fun load(): Boolean {
        if (!preferencePath.exists()) {
            flush()
            return true
        }
        return try {
            preferencePath.inputStream().use {
                json.decodeFromStream<Map<String, Map<String, String>>>(it)
            }.forEach { (k, v) -> preferences[k] = v.toMutableMap() }
            flush()
            true
        } catch (e: Exception) {
            logger.error("Error reading preferences: ", e)
            false
        }
    }

    fun flush() {
        preferencePath.deleteIfExists()
        preferencePath.createFile()
        preferencePath.writeText(json.encodeToString(preferences))
    }

    fun computeIfAbsent(context: Context, key: String, fn: Context.(String) -> String): String {
        return preferences.computeIfAbsent(context.descriptor()) { mutableMapOf() }
            .computeIfAbsent(key) { context.fn(it) }
    }

    fun set(context: Context, key: String, value: String) {
        preferences.computeIfAbsent(context.descriptor()) { mutableMapOf() }[key] = value
    }

    fun remove(context: Context) {
        preferences.remove(context.descriptor())
    }

    fun removeKey(context: Context, key: String) {
        preferences.computeIfAbsent(context.descriptor()) { mutableMapOf() }.remove(key)
    }

    fun get(context: Context, key: String): String? {
        return preferences.computeIfAbsent(context.descriptor()) { mutableMapOf() }[key]
    }
}

fun String.parseKeyPair(): Pair<String, String>? {
    if (this.contains("=")) {
        val splited = this.split("=")
        if (splited.size < 2) return null
        return (splited[0] to splited.subList(0, splited.size).joinToString("=", "", ""))
    }
    return null
}