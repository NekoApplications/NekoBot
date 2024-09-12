package icu.takeneko.nekobot.command.minecraft

import icu.takeneko.nekobot.crashreport.CrashReportTranslator
import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.message.MessageResponse
import icu.takeneko.nekobot.message.MessageType
import icu.takeneko.nekobot.util.MCLogsAccess

class TranslateCrashReportCommand : Command() {

    override val commandPrefix: String
        get() = "!tr"

    override val helpMessage: String
        get() = "!tr [{<ReportId> | <ReportUrl>} | p] Optional[<version> | latest | latestStable]\n" +
            "- Use `!tr <ReportUrl> <Version>` to translate your crashreport\n" +
            "- `<ReportUrl>` indicates a url you got after uploading a crash report into https://mclo.gs\n" +
            "- `<Version>` indicates a minecraft version, specify a minecraft version is optional"+
            "- `!tr p` shows recent crashreport translation progress"

    override fun handle(commandMessage: CommandMessage): MessageResponse? {
        val arg = commandMessage[0] ?: return null
        val version = commandMessage[1] ?: "latest"
        if (arg == "p") {
            return commandMessage.createResponse {
                val jobs = CrashReportTranslator.jobs.filter {
                    if (commandMessage.from == MessageType.PRIVATE)
                        it.source?.message?.source == commandMessage.message.source
                    else
                        it.source?.message?.group == commandMessage.message.group
                }
                +"**Recent Translate Jobs (${jobs.size})**"
                +""
                jobs.forEach {
                    +it.describe()
                }
            }
        }
        val id = if (arg.startsWith("https://mclo.gs/")) arg.removePrefix("https://mclo.gs/") else arg
        try {
            val content = MCLogsAccess.getLogContentById(id)
            val job = CrashReportTranslator.submit(commandMessage, version, content)
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