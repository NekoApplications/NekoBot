package icu.takeneko.nekobot.config

import kotlinx.serialization.Serializable
import icu.takeneko.nekobot.util.gson
import kotlin.io.path.*
import kotlin.properties.Delegates

@Serializable
data class Config(
    val operator: MutableList<String> = mutableListOf(),
    val allowedGroup: MutableList<String> = mutableListOf(),
    val groupRules: MutableMap<String, MutableList<String>> = mutableMapOf()
)

lateinit var config: Config
val configPath = Path("config.json")
fun loadConfig(){
    if (!configPath.exists()){
        configPath.deleteIfExists()
        configPath.createFile()
        configPath.writer().use {
            gson.toJson(Config(), it)
        }
    }
    config = configPath.reader().use {
        gson.fromJson(it,Config::class.java)
    }
}

fun saveConfig(){
    configPath.deleteIfExists()
    configPath.createFile()
    configPath.writer().use {
        gson.toJson(config, it)
    }
}