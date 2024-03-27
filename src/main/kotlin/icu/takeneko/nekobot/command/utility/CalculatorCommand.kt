package icu.takeneko.nekobot.command.utility

import com.github.murzagalin.evaluator.Evaluator
import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.message.MessageResponse

class CalculatorCommand : Command() {

    override val commandPrefix: String
        get() = "!calc"

    override val helpMessage: String
        get() = "!calc <expression>"

    private val evaluator = Evaluator()

    override fun handle(commandMessage: CommandMessage): MessageResponse {
        return commandMessage.createResponse() {
            val expression = commandMessage.args.run {
                if (isEmpty()){
                    +helpMessage
                    return@createResponse
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
        }
    }
}