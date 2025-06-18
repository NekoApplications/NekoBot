package icu.takeneko.nekobot.i18n

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream

private const val bundledResourcePath = "data/"
private const val overrideResourcePath = "data/override/"

class Resource(private val name: String) {
    val data = mutableMapOf<String, String>()
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun read() {
        (Resource::class.java.classLoader.getResourceAsStream("$bundledResourcePath$name.json")
            ?: throw IllegalArgumentException("Could not read resource $bundledResourcePath$name.json as it does not exist."))
            .use {
                json.decodeFromStream<Map<String, String>>(it)
            }.forEach { (k, v) ->
                data[k] = v
            }
        Path("$overrideResourcePath$name").run {
            return@run if (exists()) {
                inputStream()
            } else {
                null
            }
        }?.apply {
            use {
                json.decodeFromStream<Map<String, String>>(it)
            }.forEach { (k, v) ->
                data[k] = v
            }
        }
    }

    operator fun get(key: String): String? {
        return data[key]
    }
}