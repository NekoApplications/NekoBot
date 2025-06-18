package icu.takeneko.nekobot.command

import icu.takeneko.nekobot.Environment
import icu.takeneko.nekobot.config.GroupRuleSetting
import icu.takeneko.nekobot.message.CommandContext
import icu.takeneko.nekobot.message.MessageResponseCreationScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CommandManager(private val commandPrefix: String) {
    val commands = mutableMapOf<String, Command>()
    val logger: Logger = LoggerFactory.getLogger("CommandManager")
    fun register(command: Command) {
        commands[command.commandPrefix] = command
    }

    fun run(context: CommandContext): MessageResponseCreationScope? {
        val commandMessage = CommandMessage(context)
        if (!commandMessage.commandPrefix.startsWith(commandPrefix)) return null

        if (commands.containsKey(commandMessage.commandPrefix)) {
            if (context.isGroupMessage() && Environment.permissionManagementEnabled) {
                if (!GroupRuleSetting.botEnabledFor(context.describeGroup())) {
                    return null
                }
                if (!GroupRuleSetting.commandEnabledFor(context.describeGroup(), commandMessage.commandPrefix)) {
                    return null
                }
            }
            try {
                return commands[commandMessage.commandPrefix]!!(commandMessage)
            } catch (_: CommandIgnoredException) {
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