package icu.takeneko.nekobot.command.status

import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.message.builder.MessageCreator
import icu.takeneko.nekobot.util.getVersionInfoString

class PingCommand : Command() {

    override val commandPrefix: String = "ping"
    override val helpMessage: String = "ping"

    override fun handle(commandMessage: CommandMessage): MessageCreator =
        commandMessage.createResponse {
            +"PONG ${System.currentTimeMillis()} NekoBot ${getVersionInfoString()}"
        }
}