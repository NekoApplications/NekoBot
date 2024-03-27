package icu.takeneko.nekobot.command.management

import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.config.GroupRuleSetting
import icu.takeneko.nekobot.config.config
import icu.takeneko.nekobot.message.Message
import icu.takeneko.nekobot.message.MessageResponse
import icu.takeneko.nekobot.message.MessageType

class GroupCommand : Command() {

    override val commandPrefix: String
        get() = "!g"

    override val helpMessage: String
        get() = "!g [enable | disable | e | d] <group> "

    override fun handle(commandMessage: CommandMessage): Message {
        if (commandMessage.from != MessageType.PRIVATE || commandMessage.scene !in config.operator) {
            return Message(commandMessage.scene, commandMessage.from, "", status = false, forward = false)
        }
        return MessageResponse(commandMessage.scene, commandMessage.from) {
            if (commandMessage.args.isEmpty()) {
                +helpMessage
                return@MessageResponse
            }
            val action = commandMessage[0] ?: run {
                +"Expected action: [enable | disable | e | d]"
                return@MessageResponse
            }
            val group = commandMessage[1] ?: run {
                +"Expected group"
                return@MessageResponse
            }
            when (action) {
                "e", "enable" -> {
                    GroupRuleSetting.enableBotForGroup(group)
                    +"Enabled bot for group $group"
                }

                "d", "disable" -> {
                    GroupRuleSetting.disableBotForGroup(group)
                    +"Disabled bot for group $group"
                }

                else -> {
                    +"Expected action: [enable | disable | e | d]"
                }
            }
        }.toMessage()
    }
}