package icu.takeneko.nekobot.command.status

import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.message.MessageResponseCreationScope
import icu.takeneko.nekobot.util.getVersionInfoString

class StatusCommand : Command() {

    override val commandPrefix: String = "!stat"
    override val helpMessage: String = "!stat"

    override fun handle(commandMessage: CommandMessage): MessageResponseCreationScope =
        commandMessage.createResponse {
            +"${System.currentTimeMillis()} NekoBot ${getVersionInfoString()}"
        }
}