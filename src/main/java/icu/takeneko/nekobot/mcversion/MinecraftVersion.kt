package icu.takeneko.nekobot.mcversion

import icu.takeneko.nekobot.util.configureProxyIfPossible
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

object MinecraftVersion {
    private val mojangApiUrl = "https://piston-meta.mojang.com/"
    private val versionManifestUrl = "$mojangApiUrl/mc/game/version_manifest.json"
    private lateinit var versionManifest: VersionManifest
    lateinit var latestStableVersion: String
    lateinit var latestVersion: String
    val versions = mutableMapOf<String, VersionData>()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation){
            json(json = json)
        }
        engine{
            configureProxyIfPossible()
        }
    }

    fun update() {
        runBlocking {
            val response = client.get(versionManifestUrl)
            versionManifest = response.body<VersionManifest>()
            versions.clear()
            versions += versionManifest.versions.map { v -> v.id to v }
            latestStableVersion = versionManifest.latest.release
            latestVersion = versionManifest.latest.snapshot
        }
    }

    operator fun get(minecraftVersion: String): VersionData? {
        return versions[minecraftVersion]
    }
}
@kotlinx.serialization.Serializable
data class LatestData(val release: String, val snapshot: String)

@kotlinx.serialization.Serializable
@Suppress("UNUSED")
enum class VersionType {
    @kotlinx.serialization.SerialName("snapshot")
    SNAPSHOT,

    @kotlinx.serialization.SerialName("release")
    RELEASE,

    @kotlinx.serialization.SerialName("old_alpha")
    OLD_ALPHA,

    @kotlinx.serialization.SerialName("old_beta")
    OLD_BETA,

}

@kotlinx.serialization.Serializable
data class VersionData(
    val id: String,
    val type: VersionType,
    val url: String,
    val releaseTime: String,
    val time: String
)

@kotlinx.serialization.Serializable
data class VersionManifest(val latest: LatestData, val versions: MutableList<VersionData>)