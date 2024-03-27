package icu.takeneko.nekobot.command.minecraft

import icu.takeneko.nekobot.command.Command
import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.mcversion.MinecraftVersion
import icu.takeneko.nekobot.message.Message
import icu.takeneko.nekobot.message.MessageResponse

class MinecraftVersionCommand : Command() {

    override val commandPrefix: String
        get() = "!mv"

    override val helpMessage: String
        get() = "!mv Optional[<version> | latest | latestStable]"

    override fun handle(commandMessage: CommandMessage): Message {
        return MessageResponse(commandMessage.scene, commandMessage.from) {
            +"**Minecraft Version**"
            +""
            val version = commandMessage[0] ?: run {
                +"**Latest Stable Version:** ${MinecraftVersion.latestStableVersion}"
                +"**Latest Snapshot Version:** ${MinecraftVersion.latestVersion}"
                return@MessageResponse
            }
            val minecraftVersion = resolve(version) ?: run {
                +"Expected version: [<version> | latest | latestStable]"
                return@MessageResponse
            }
            val versionData = MinecraftVersion[minecraftVersion]!!
            +"**Version:** ${versionData.id}"
            +"**Version Type:** ${versionData.type}"
            +"**Release Time:** ${versionData.releaseTime}"
            +"**Version Json Download Url:** ${versionData.url}"
        }.toMessage()
    }

    private fun resolve(version: String?) =
        when (version) {
            "latest" -> MinecraftVersion.latestVersion
            "latestStable" -> MinecraftVersion.latestStableVersion
            null -> null
            else -> if (version in MinecraftVersion.versions.keys) version else null
        }
}