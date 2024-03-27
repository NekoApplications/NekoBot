package icu.takeneko.nekobot.command.minecraft

import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.message.Message
import icu.takeneko.nekobot.message.MessageResponse

class YarnClassCommand : Command() {
    override val commandPrefix: String = "!yc"
    override val helpMessage: String = "!yc <className> Optional[<version> | latest | latestStable]"
    override fun handle(commandMessage: CommandMessage): MessageResponse? {
        return commandMessage.createResponse() {
            if (commandMessage.args.isEmpty()){
                + helpMessage
                return@createResponse
            }
            val version = versionRepository.resolve(commandMessage[1]) ?: run {
                +"Expected Minecraft Version"
                return@createResponse
            }
            val data = mappingRepository.getMappingData(version)
            val className = commandMessage[0] ?: run {
                +"Expected Class Name"
                return@createResponse
            }
            val results = data.findClasses(className, data.resolveNamespaces(namespaces, false))
            if (results.isEmpty()){
                + "no matches for the given class name, MC version and query namespace"
                return@createResponse
            }
            +"${data.mcVersion} matches"
            for (result in results) {
                +"**Names**"
                +""
                for (namespace in namespaces) {
                    val res = result.getName(namespace) ?: continue
                    + "**$namespace**: $res"
                }
                +""
                + "**Yarn Access Widener**: accessible\tclass\t${result.getName("yarn")}"
            }
            +"query ns: ${namespaces.joinToString(",")}"
        }
    }
}