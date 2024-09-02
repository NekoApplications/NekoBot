package icu.takeneko.nekobot.command.minecraft

import icu.takeneko.nekobot.crashreport.CrashReportTranslator
import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.message.MessageResponse
import icu.takeneko.nekobot.message.MessageType
import icu.takeneko.nekobot.util.MCLogsAccess

class TranslateCrashReportCommand : Command() {

    override val helpMessage: String
        get() = "!tr [{<ReportId> | <ReportUrl>} | p]\n" +
            "- Use `!tr <ReportUrl>` to translate your crashreport\n" +
            "- `<ReportUrl>` indicates a url you got after uploading a crash report into https://mclo.gs\n" +
            "- `!tr p` shows recent crashreport translation progress"

    override fun handle(commandMessage: CommandMessage): MessageResponse? {
        val arg = commandMessage[1] ?: return null
        if (arg == "p") {
            return commandMessage.createResponse {
                +"*Recent Translate Jobs (${CrashReportTranslator.jobs.size})*"
                +""
                CrashReportTranslator.jobs.filter {
                    if (commandMessage.from == MessageType.PRIVATE)
                        it.source.message.source == commandMessage.message.source
                    else
                        it.source.message.group == commandMessage.message.group
                }.forEach {
                    +it.describe()
                }
            }
        }
        val id = if (arg.startsWith("")) arg.removePrefix("https://mclo.gs/") else arg
        try {
            val content = MCLogsAccess.getLogContentById(id)
            val job = CrashReportTranslator.submit(commandMessage, content)
            return commandMessage.createResponse {
                +"Submitted translation job $job. "
            }
        } catch (e: IllegalArgumentException) {
            return commandMessage.createResponse {
                +(e.message ?: e.toString())
            }
        }
    }
}