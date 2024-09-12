package icu.takeneko.nekobot.crashreport

import icu.takeneko.nekobot.command.CommandMessage
import icu.takeneko.nekobot.command.minecraft.mappingRepository
import icu.takeneko.nekobot.command.minecraft.namespaces
import icu.takeneko.nekobot.command.minecraft.versionRepository
import icu.takeneko.nekobot.message.MessageResponse
import icu.takeneko.nekobot.message.MessageType
import icu.takeneko.nekobot.util.MCLogsAccess
import org.slf4j.LoggerFactory
import java.util.regex.Pattern
import kotlin.io.path.Path
import kotlin.io.path.readText

class CrashReportTranslationJob(
    val source: CommandMessage?,
    private val version: String,
    private val content: String
) {
    private val logger = LoggerFactory.getLogger("CrashReportTranslationJob-${toString()}")
    private val symbolPattern = Pattern.compile("at ((([a-zA-Z0-9\$_]+)\\.)+)([<>a-zA-Z0-9\$_]+)").toRegex()
    private val modLoaderPattern = Pattern.compile("Is Modded: Definitely; (Client|Server) brand changed to '(.+)'").toRegex()
    fun describe(): String {
        return "${toString()} ${source?.message?.source?.nick ?: ""} ${stage.formatter(this)}"
    }

    private var stage = TranslationStage.DETERMINE_SYMBOL
    val classSymbols = mutableMapOf<String, String>()
    val methodSymbols = mutableSetOf<Pair<String, String>>()
    val translatedClassSymbols = mutableMapOf<String, String>()
    val translatedMethodSymbols = mutableMapOf<String, String>()
    var mcLogsId: String? = null
    private var translatedContent: String? = null

    override fun toString(): String {
        return Integer.toHexString(hashCode())
    }

    private fun determineNamespace(): String {
        if (content.contains("Fabric Mods"))return "yarn"
        val match = modLoaderPattern.find(content) ?: return "mojmap"
        val modLoader = (match.groups[2] ?: return "mojmap").value
        return if (modLoader.contains("fabric")) "yarn" else "mojmap"
    }

    suspend fun CommandMessage.respond(fn: MessageResponse.() -> Unit) {
        val ret = createResponse(fn)
        when (this.from) {
            MessageType.GROUP -> {
                ret.source.group!!.sendMessage(ret.asMessageChain())
            }

            MessageType.PRIVATE -> {
                ret.source.source.sendMessage(ret.asMessageChain())
            }
        }
    }

    suspend fun run() {
        try {
            this.stage = TranslationStage.DETERMINE_SYMBOL
            var match = symbolPattern.find(content)
            while (match != null) {
                val classFullName = match.groups[1] ?: continue
                val clazzName = match.groups[3] ?: continue
                val methodName = match.groups[4] ?: continue
                val classNamePath = classFullName.value.removeSuffix(".").replace(".", "/")
                classSymbols[classNamePath] = clazzName.value
                methodSymbols += classNamePath to methodName.value
                match = match.next()
            }
            logger.info("Found ${methodSymbols.size} methods.")
            logger.info("Found ${classSymbols.size} classes.")
            this.stage = TranslationStage.TRANSLATING
            val version = versionRepository.resolve(version)
            if (version == null) {
                source?.run {
                    this.respond {
                        +"Translation job ${toString()} from ${this.source.source.nick} finished."
                        +"Error: Invalid minecraft version (${this@CrashReportTranslationJob.version})."
                    }
                }
                return
            }

            val targetNamespace = determineNamespace()
            logger.info("Determined namespace $targetNamespace")
            val mappingData = mappingRepository.getMappingData(version)
            for (entry in classSymbols) {
                val (classFullName, className) = entry
                val results = mappingData.findClasses(className, mappingData.resolveNamespaces(namespaces, false))
                val matchResult = results.find {
                    for (ns in namespaces) {
                        val res = it.getName(ns) ?: continue
                        if (res == classFullName) {
                            return@find true
                        }
                    }
                    return@find false
                } ?: continue
                val trResult = matchResult.getName(targetNamespace)
                if (trResult != null) {
                    translatedClassSymbols[classFullName] = trResult
                }
            }
            logger.info("Translated ${translatedClassSymbols.size} classes.")
            for ((ownerClass, methodName) in methodSymbols) {
                val results = mappingData.findMethods(methodName, mappingData.resolveNamespaces(namespaces, false))
                val matchResult = results.find {
                    var hasValidOwner = false
                    for (ns in namespaces) {
                        val owner = it.owner
                        if ((owner.getName(ns) ?: continue) == ownerClass) {
                            hasValidOwner = true
                            break
                        }
                    }
                    if (!hasValidOwner) return@find false
                    for (ns in namespaces) {
                        if ((it.getName(ns) ?: continue) == methodName) {
                            return@find true
                        }
                    }
                    false
                } ?: continue
                val trResult = matchResult.getName(targetNamespace)
                if (trResult != null) {
                    translatedMethodSymbols[methodName] = trResult
                }
            }
            logger.info("Translated ${translatedMethodSymbols.size} methods.")
            val classReplacement = mutableMapOf<String, String>()
            var newContent = content
            translatedClassSymbols.forEach { (t, u) ->
                classReplacement[t.replace("/", ".")] = u.replace("/", ".")
            }
            classReplacement.forEach { (t, u) ->
                newContent = newContent.replace(t, u)
            }
            translatedMethodSymbols.forEach { (t, u) ->
                newContent = newContent.replace(t, u)
            }
            this.translatedContent = newContent
            println(newContent)
            stage = TranslationStage.UPLOADING
            mcLogsId = MCLogsAccess.updateLogContent(newContent)
            logger.info("Uploaded translated crash report to https://mclo.gs/$mcLogsId")
            source?.run {
                this.respond {
                    +"Translation job ${this@CrashReportTranslationJob} from ${this.source.source.nick} finished."
                    +"Translated crash report has been uploaded to https://mclo.gs/$mcLogsId"
                }
            }
            stage = TranslationStage.DONE
        } catch (e: Exception) {
            e.printStackTrace()
            source?.run {
                this.respond {
                    +"Translation job ${toString()} from ${this.source.source.nick} finished with an exception."
                    +e.toString()
                }
            }
        }
    }
}
private val modLoaderPattern = Pattern.compile("Is Modded: Definitely; (Client|Server) brand changed to '(.+)'").toRegex()

fun determineNamespace(content: String): String {
    if (content.contains("Fabric Mods"))return "yarn"
    val match = modLoaderPattern.find(content) ?: return "mojmap"
    println(match)
    val modLoader = (match.groups[2] ?: return "mojmap").value
    return if (modLoader.contains("fabric")) "yarn" else "mojmap"
}

fun main() {
    println(determineNamespace(Path("crash-2023-09-24_23.11.03-server.txt").readText()))
}