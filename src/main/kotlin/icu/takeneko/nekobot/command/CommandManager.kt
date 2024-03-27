package icu.takeneko.nekobot.command

import icu.takeneko.nekobot.config.GroupRuleSetting
import icu.takeneko.nekobot.message.Message
import icu.takeneko.nekobot.message.MessageResponse
import icu.takeneko.nekobot.message.MessageType
import org.slf4j.LoggerFactory

object CommandManager {
    val commands = mutableMapOf<String, Command>()
    val logger = LoggerFactory.getLogger("CommandManager")
    fun register(command: Command) {
        commands[command.commandPrefix] = command
    }

    fun run(input: Message): MessageResponse? {
        val commandMessage = CommandMessage(input)
        return if (commands.containsKey(commandMessage.commandPrefix)) {
            if (commandMessage.from == MessageType.GROUP) {
                if (!GroupRuleSetting.botEnabledFor(input.group!!.id.toString())) {
                   return null
                }
                if (!GroupRuleSetting.commandEnabledFor(input.group.id.toString(), commandMessage.commandPrefix)) {
                    return null
                }
            }
            try {
                commands[commandMessage.commandPrefix]!!(commandMessage)
            } catch (e: Exception) {
                logger.error("Exception occurred while running command ${input.messagePlain}", e)
                return MessageResponse(input) {
                    +"Server Internal Error."
                    +input.messagePlain
                    +"~~~"
                    +e.toString()
                }
            }
        } else {
            return null
        }
    }

    val commandPrefixes
        get() = commands.keys
}