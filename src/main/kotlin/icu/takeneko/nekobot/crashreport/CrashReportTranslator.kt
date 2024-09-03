package icu.takeneko.nekobot.crashreport

import icu.takeneko.nekobot.command.CommandMessage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object CrashReportTranslator {
    val jobs = mutableListOf<CrashReportTranslationJob>()

    @OptIn(DelicateCoroutinesApi::class)
    fun submit(source:CommandMessage, version:String, content:String):CrashReportTranslationJob {
        return CrashReportTranslationJob(source, version, content).also{
            jobs.add(it)
            GlobalScope.launch {
                it.run()
            }
        }
    }
}