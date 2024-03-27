package icu.takeneko.nekobot.command.minecraft

import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.message.Message
import icu.takeneko.nekobot.message.MessageResponse

class YarnFieldCommand : Command() {
    override val commandPrefix: String
        get() = "!yf"

    override val helpMessage: String
        get() = "!yf <fieldName> Optional[<version> | latest | latestStable]"

    override fun handle(commandMessage: CommandMessage): Message {
        return MessageResponse(commandMessage.scene, commandMessage.from) {
            if (commandMessage.args.isEmpty()) {
                +helpMessage
                return@MessageResponse
            }
            val version = versionRepository.resolve(commandMessage[1]) ?: run {
                +"Expected Minecraft Version"
                return@MessageResponse
            }
            val data = mappingRepository.getMappingData(version)
            val className = commandMessage[0] ?: run {
                +"Expected Method Name"
                return@MessageResponse
            }
            val results = data.findFields(className, data.resolveNamespaces(namespaces, false))
            if (results.isEmpty()) {
                +"no matches for the given method name, MC version and query namespace"
                return@MessageResponse
            }
            +"$version matches"
            for (result in results) {
                +"**Class Names**"
                +""
                for (namespace in namespaces) {
                    +"**$namespace:** ${result.owner.getName(namespace) ?: continue}"
                }
                +""
                +"**Field Names**"
                for (namespace in namespaces) {
                    +"**$namespace:** ${result.getName(namespace) ?: continue}"
                }
                +""
                +String.format(
                    """
                            **Yarn Field Descriptor**

                            %3${'$'}s
                            **Yarn Access Widener**

                            accessible${'\t'}method${'\t'}%1${'$'}s${'\t'}%2${'$'}s${'\t'}%3${'$'}s
                            **Yarn Mixin Target**

                            L%1${'$'}s;%2${'$'}s%3${'$'}s

                            """.trimIndent(),
                    result.owner.getName("yarn") ?: continue,
                    result.getName("yarn") ?: continue,
                    result.getDesc("yarn") ?: continue
                )

            }
            +"query ns: ${namespaces.joinToString(",")}"
        }.toMessage(forward = true)
    }
}