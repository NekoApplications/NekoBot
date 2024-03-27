package icu.takeneko.nekobot.command.management

import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandManager
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.config.GroupRuleSetting
import icu.takeneko.nekobot.config.config
import icu.takeneko.nekobot.message.Message
import icu.takeneko.nekobot.message.MessageResponse
import icu.takeneko.nekobot.message.MessageType

class GroupRuleCommand : Command() {

    override val commandPrefix: String
        get() = "!gr"

    override val helpMessage: String
        get() = "!gr [enable | disable | e | d | p] <group> [<command> | ALL]"

    override fun handle(commandMessage: CommandMessage): MessageResponse? {
        if (commandMessage.from != MessageType.PRIVATE || commandMessage.sender !in config.operator) {
            return null
        }
        return commandMessage.createResponse {
            if (commandMessage.args.isEmpty()) {
                +helpMessage
                return@createResponse
            }
            val action = commandMessage[0] ?: run {
                +"Expected action: [enable | disable | e | d]"
                return@createResponse
            }
            if (action == "p"){
                +"**Group Settings**"
                +""
                +"**Enabled Group:** ${config.allowedGroup.joinToString(", ")}"
                +""
                +"**Enabled Commands For Group:**"
                config.groupRules.forEach {
                    +"${it.key}: ${it.value.joinToString(", ")}"
                }
                return@createResponse
            }
            val group = commandMessage[1] ?: run {
                +"Expected group"
                return@createResponse
            }
            val command = commandMessage[2] ?: run {
                +"Expected command"
                return@createResponse
            }

            when (action) {
                "e", "enable" -> {
                    if (command == "ALL") {
                        for (c in CommandManager.commandPrefixes) {
                            GroupRuleSetting.enableCommandForGroup(group, c)
                            +"Enabled command $c for group $group"
                        }
                        return@createResponse
                    }
                    if ("+" in command) {
                        command.split("+").forEach {
                            if (it !in CommandManager.commands) {
                                +"Command $it not registered."
                                return@forEach
                            }
                            GroupRuleSetting.enableCommandForGroup(group, it)
                            +"Enabled command $it for group $group"
                        }
                        return@createResponse
                    }
                    if (command !in CommandManager.commands) {
                        +"Command $command not registered."
                        return@createResponse
                    }
                    GroupRuleSetting.enableCommandForGroup(group, command)
                    +"Enabled command $command for group $group"
                }

                "d", "disable" -> {
                    if (command == "ALL") {
                        for (c in CommandManager.commandPrefixes) {
                            GroupRuleSetting.enableCommandForGroup(group, c)
                            +"Disabled command $c for group $group"
                        }
                        return@createResponse
                    }
                    if ("+" in command) {
                        command.split("+").forEach {
                            if (it !in CommandManager.commands) {
                                +"Command $it not registered."
                                return@forEach
                            }
                            GroupRuleSetting.disableCommandForGroup(group, it)
                            +"Disabled command $it for group $group"
                        }
                        return@createResponse
                    }
                    if (command !in CommandManager.commands) {
                        +"Command $command not registered."
                        return@createResponse
                    }
                    GroupRuleSetting.disableCommandForGroup(group, command)
                    +"Disabled command $command for group $group"
                }
                else -> {
                    +"Expected action: [enable | disable | e | d | p]"
                }
            }
        }
    }
}