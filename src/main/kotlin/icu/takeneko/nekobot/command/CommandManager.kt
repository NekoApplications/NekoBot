package icu.takeneko.nekobot.command

import icu.takeneko.nekobot.config.GroupRuleSetting
import icu.takeneko.nekobot.message.CommandContext
import icu.takeneko.nekobot.message.MessageResponseCreationScope
import icu.takeneko.nekobot.message.MessageType
import org.slf4j.LoggerFactory

object CommandManager {
    val commands = mutableMapOf<String, Command>()
    val logger = LoggerFactory.getLogger("CommandManager")
    fun register(command: Command) {
        commands[command.commandPrefix] = command
    }

    fun run(context: CommandContext): MessageResponseCreationScope? {
        val commandMessage = CommandMessage(context)
        return if (commands.containsKey(commandMessage.commandPrefix)) {
            if (context.messageType == MessageType.GROUP) {
                if (!GroupRuleSetting.botEnabledFor(context.group!!.id.toString())) {
                    return null
                }
                if (!GroupRuleSetting.commandEnabledFor(context.group.id.toString(), commandMessage.commandPrefix)) {
                    return null
                }
            }
            try {
                commands[commandMessage.commandPrefix]!!(commandMessage)
            } catch (e: CommandIgnoredException) {
                return null
            } catch (e: Exception) {
                logger.error("Exception occurred while running command ${context.messagePlain}", e)
                return MessageResponseCreationScope(context) {
                    +"Server Internal Error."
                    +context.messagePlain
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