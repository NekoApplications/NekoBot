package icu.takeneko.nekobot.command.management

import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandManager
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.config.GroupRuleSetting
import icu.takeneko.nekobot.message.MessageResponseCreationScope
import icu.takeneko.nekobot.message.MessageType

class HelpCommand : Command() {
    override val commandPrefix: String
        get() = "help"

    override val helpMessage: String
        get() = "help"

    override fun handle(commandMessage: CommandMessage): MessageResponseCreationScope {
//        if (commandMessage.from != MessageType.PRIVATE || commandMessage.scene !in config.operator) {
//            return Message(commandMessage.scene, commandMessage.from, "", status = false, forward = false)
//        }
        return commandMessage.createResponse {
            +"**Commands**"
            +""
            commandMessage.context.manager().commands.forEach {
                if (commandMessage.context.messageType == MessageType.PRIVATE
                    || GroupRuleSetting.commandEnabledFor(commandMessage.context.describeGroup(), it.key)
                ) {
                    +it.value.helpMessage
                }
            }
        }
    }
}