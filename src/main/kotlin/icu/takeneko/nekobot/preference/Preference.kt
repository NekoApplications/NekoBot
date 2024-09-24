package icu.takeneko.nekobot.preference

import icu.takeneko.nekobot.Context

object Preference {
    val preferences: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    fun set(context: Context, key: String, value: String) {
        preferences.computeIfAbsent(context.descriptor()){ mutableMapOf()}[key] = value
    }

    fun remove(context: Context){
        preferences.remove(context.descriptor())
    }

    fun removeKey(context: Context, key: String){
        preferences.computeIfAbsent(context.descriptor()){ mutableMapOf()}.remove(key)
    }

    fun get(context: Context, key: String): String? {
       return preferences.computeIfAbsent(context.descriptor()){ mutableMapOf()}[key]
    }
}