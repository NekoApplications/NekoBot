package icu.takeneko.nekobot.command.minecraft

import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.message.MessageResponseCreationScope

class VersionCacheCommand : Command() {

    override val commandPrefix: String
        get() = "vc"

    override val helpMessage: String
        get() = "vc [p | {[a | add}] [<version> | latest | latestStable]}]"

    override fun handle(commandMessage: CommandMessage): MessageResponseCreationScope {
        return commandMessage.createResponse {
            val scene = commandMessage[0] ?: run {
                + "`$helpMessage`"
                return@createResponse
            }
            when (scene) {
                "a","add" -> {
                    val version = versionRepository.resolve(commandMessage[1]) ?: run {
                        + "Expected version: [<version> | latest | latestStable]"
                        return@createResponse
                    }
                    if (version !in mappingRepository.cachedVersions) {
                        mappingRepository.getMappingData(version)
                        +"Created Mapping cache for version $version"
                        return@createResponse
                    }
                    +"Version $version already cached."
                }
                "p" -> {
                    +"**Cached Version Mapping Data**"
                    +""
                    for (version in mappingRepository.cachedVersions) {
                        +version
                    }
                }
                else -> + "`$helpMessage`"
            }
        }
    }
}