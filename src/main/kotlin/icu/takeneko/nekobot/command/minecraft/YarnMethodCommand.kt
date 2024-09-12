package icu.takeneko.nekobot.command.minecraft

import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.message.MessageResponse
import icu.takeneko.nekobot.util.getDescOrElse
import icu.takeneko.nekobot.util.getNameOrElse

class YarnMethodCommand : Command() {

    override val commandPrefix: String
        get() = "!ym"

    override val helpMessage: String
        get() = "!ym <methodName> Optional[<version> | latest | latestStable]"

    override fun handle(commandMessage: CommandMessage): MessageResponse? {
        return commandMessage.createResponse {
            if (commandMessage.args.isEmpty()) {
                +helpMessage
                return@createResponse
            }
            val version = versionRepository.resolve(commandMessage[1]) ?: run {
                +"Expected Minecraft Version"
                return@createResponse
            }
            val data = mappingRepository.getMappingData(version)
            val methodName = commandMessage[0] ?: run {
                +"Expected Method Name"
                return@createResponse
            }
            val results = data.findMethods(methodName, data.resolveNamespaces(namespaces, false))
            if (results.isEmpty()) {
                +"no matches for the given method name, MC version and query namespace"
                return@createResponse
            }
            +"$version matches"
            for (result in results) {
                +"**Class Names**"
                +""
                for (namespace in namespaces) {
                    +"**$namespace:** ${result.owner.getName(namespace) ?: continue}"
                }
                +""
                +"**Method Names**"
                for (namespace in namespaces) {
                    +"**$namespace:** ${result.getName(namespace) ?: continue}"
                }
                +""
                +String.format(
                    """
                            **Yarn Method Descriptor**

                            %3${'$'}s
                            **Yarn Access Widener**

                            accessible${'\t'}method${'\t'}%1${'$'}s${'\t'}%2${'$'}s${'\t'}%3${'$'}s
                            **Yarn Mixin Target**

                            L%1${'$'}s;%2${'$'}s%3${'$'}s

                            """.trimIndent(),
                    result.owner.getName("yarn")?: continue,
                    result.getName("yarn")?: continue,
                    result.getDesc("yarn")?: continue
                )
                +"**Access Transformer**"
                +"public ${result.owner.getNameOrElse("mcp", "mojmap")} ${
                    result.getNameOrElse(
                        "mcp",
                        "mojmap"
                    )
                }${result.getDescOrElse("mcp", "mojmap")}"
            }
            +"query ns: ${namespaces.joinToString(",")}"
        }
    }
}