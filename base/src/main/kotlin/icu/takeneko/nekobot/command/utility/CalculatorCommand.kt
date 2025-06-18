package icu.takeneko.nekobot.command.utility

import com.github.murzagalin.evaluator.DefaultFunctions
import com.github.murzagalin.evaluator.Evaluator
import com.github.murzagalin.evaluator.Function
import com.github.murzagalin.evaluator.OneNumberArgumentFunction
import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.message.MessageResponseCreationScope
import java.security.SecureRandom

class CalculatorCommand : Command() {

    override val commandPrefix: String
        get() = "calc"

    override val helpMessage: String
        get() = "calc <expression>"

    private val evaluator = Evaluator(DefaultFunctions.ALL + listOf(
        object : Function("rand", 0..0) {
            val random = SecureRandom()
            override fun invoke(vararg args: Any): Any {
                require(args.isEmpty()){"Function rand() has no arguments"}
                return random.nextFloat()
            }
        },
        object :OneNumberArgumentFunction("int", 0..1){
            override fun invokeInternal(arg: Number): Double {
                return arg.toInt().toDouble()
            }
        }
    ))

    override fun handle(commandMessage: CommandMessage): MessageResponseCreationScope {
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