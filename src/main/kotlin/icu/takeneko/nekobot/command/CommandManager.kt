package icu.takeneko.nekobot.command

import icu.takeneko.nekobot.config.GroupRuleSetting
import icu.takeneko.nekobot.message.Message
import icu.takeneko.nekobot.message.MessageResponse
import icu.takeneko.nekobot.message.MessageType
import org.slf4j.LoggerFactory

object CommandManager {
    val commands = mutableMapOf<String,Command>()
    val logger = LoggerFactory.getLogger("CommandManager")
    fun register(command: Command) {
        commands[command.commandPrefix] = command
    }

    fun run(input:Message):Message{
        val commandMessage = CommandMessage(input)
        return if (commands.containsKey(commandMessage.commandPrefix)){
            if (commandMessage.from == MessageType.GROUP) {
                if (!GroupRuleSetting.botEnabledFor(commandMessage.scene)){
                    return MessageResponse(input.scene, input.messageType){
                        + "Bot is not enabled for this group."
                        + input.messagePlain
                        + "~~~"
                    }.toMessage(false)
                }
                if (!GroupRuleSetting.commandEnabledFor(commandMessage.scene, commandMessage.commandPrefix)) {
                    return MessageResponse(input.scene, input.messageType){
                        + "This command is not enabled for this group."
                        + input.messagePlain
                        + "~~~"
                    }.toMessage(false)
                }
            }
            try{
                commands[commandMessage.commandPrefix]!!(commandMessage)
            }catch (e:Exception){
                logger.error("Exception occurred while running command ${input.messagePlain}", e)
                MessageResponse(input.scene, input.messageType){
                    + "Server Internal Error."
                    + input.messagePlain
                    + "~~~"
                    + e.toString()
                }.toMessage(true)
            }
        }else{
            MessageResponse(input.scene, input.messageType){
                + "Command not found."
                + input.messagePlain
                + "~~~"
            }.toMessage(false)
        }
    }

    val commandPrefixes
        get() = commands.keys
}