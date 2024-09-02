package icu.takeneko.nekobot.crashreport

import icu.takeneko.nekobot.command.CommandMessage

class CrashReportTranslationJob(val source: CommandMessage, val content:String) {
    fun describe(): String {
        return "${toString()} ${source.message.source.nick} ${stage.formatter(this)}"
    }

    var stage = TranslationStage.DETERMINE_SYMBOL
    val symbols: List<String> = mutableListOf()
    val translatedSymbols: Map<String, String> = mutableMapOf()

    override fun toString(): String {
        return Integer.toHexString(hashCode())
    }

    suspend fun run(){

    }

}