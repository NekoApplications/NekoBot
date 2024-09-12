package icu.takeneko.nekobot.util

import com.google.gson.GsonBuilder
import net.fabricmc.mappingio.tree.MappingTree.ElementMapping
import net.fabricmc.mappingio.tree.MappingTree.MemberMapping
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

fun ElementMapping.getNameOrElse(ns1:String, ns2:String): String? {
    return getName(ns1) ?: getName(ns2)
}

fun MemberMapping.getDescOrElse(ns1:String, ns2:String): String? {
    return getDesc(ns1) ?: getDesc(ns2)
}