package icu.takeneko.nekobot.config

import icu.takeneko.nekobot.util.gson
import kotlinx.serialization.Serializable
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.reader
import kotlin.io.path.writer

@Serializable
data class Config(
    val token: String = "",
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
    configPath.writer().use {
        gson.toJson(config, it)
    }
}

fun saveConfig(){
    configPath.deleteIfExists()
    configPath.createFile()
    configPath.writer().use {
        gson.toJson(config, it)
    }
}