package icu.takeneko.nekobot.util

import com.google.gson.GsonBuilder
import java.util.*

val gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()
fun getVersionInfoString(): String {
    val version = BuildProperties["version"]
    val buildTimeMillis = BuildProperties["buildTime"]?.toLong() ?: 0L
    val buildTime = Date(buildTimeMillis)
    return "$version (${BuildProperties["branch"]}:${
        BuildProperties["commitId"]?.substring(0, 7)
    } $buildTime)"
}