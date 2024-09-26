package icu.takeneko.nekobot.command.utility

import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.message.MessageResponseCreationScope
import icu.takeneko.nekobot.preference.Preference
import icu.takeneko.nekobot.preference.parseKeyPair

class PreferenceCommand : Command() {

    override val commandPrefix: String
        get() = "!pref"

    override val helpMessage: String
        get() = "!pref [get key... | set <key=value>... | clear]"

    override fun handle(commandMessage: CommandMessage): MessageResponseCreationScope {
        return commandMessage.createResponse {
            if (commandMessage.args.isEmpty()) return@createResponse
            val verb = commandMessage.args[0]
            +"**Preferences**"
            +""
            when (verb) {
                "get" -> {
                    commandMessage.args.subList(0, commandMessage.args.size)
                        .forEach {
                            val pref = Preference.get(context, it)
                            if (pref == null) {
                                +"Could not get preference value of $it as it does not exist."
                                return@forEach
                            }
                            +"$it=$pref"
                        }
                }

                "set" -> {
                    var completed = false;
                    commandMessage.args.subList(0, commandMessage.args.size)
                        .forEach {
                            val (k, v) = it.parseKeyPair() ?: kotlin.run {
                                +"$it is not a valid key-value pair"
                                return@forEach
                            }
                            Preference.set(context, k, v)
                            completed = true
                        }
                    if (completed) {
                        +"The operation completed successfully."
                    }
                }

                "clear" -> {
                    Preference.remove(context)
                    +"The operation completed successfully."
                }

                else -> {
                    +helpMessage
                }
            }
        }
    }
}