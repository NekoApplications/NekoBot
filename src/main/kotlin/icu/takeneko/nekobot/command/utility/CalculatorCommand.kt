package icu.takeneko.nekobot.command.utility

import com.github.murzagalin.evaluator.Evaluator
import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.message.Message
import icu.takeneko.nekobot.message.MessageResponse

class CalculatorCommand : Command() {

    override val commandPrefix: String
        get() = "!calc"

    override val helpMessage: String
        get() = "!calc <expression>"

    private val evaluator = Evaluator()

    override fun handle(commandMessage: CommandMessage): Message {
        return MessageResponse(commandMessage.scene, commandMessage.from) {
            val expression = commandMessage.args.run {
                if (isEmpty()){
                    +helpMessage
                    return@MessageResponse
                }
                joinToString(" ")
            }
            try {
                +(expression + " = " + evaluator.evaluateDouble(expression).toString())
            } catch (e: Exception) {
                +expression
                +"~~~"
                +(e.message ?: "Expected expression")
            }
        }.toMessage()
    }
}