package icu.takeneko.nekobot.mcversion

import com.google.gson.annotations.SerializedName
import io.ktor.http.*
import icu.takeneko.nekobot.util.gson
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

object MinecraftVersion {

    private val mojangApiUrl = "https://piston-meta.mojang.com/"
    private val versionManifestUrl = "$mojangApiUrl/mc/game/version_manifest.json"
    private lateinit var versionManifest: VersionManifest
    private val httpClient = HttpClient.newHttpClient()
    lateinit var latestStableVersion: String
    lateinit var latestVersion: String
    val versions = mutableMapOf<String, VersionData>()
    fun update() {
        val request = HttpRequest.newBuilder().GET().uri(Url(versionManifestUrl).toURI()).build()
        httpClient.sendAsync(request, BodyHandlers.ofString()).thenAccept {
            val resp = it.body()
            versionManifest = gson.fromJson(resp, VersionManifest::class.java)
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

data class LatestData(val release: String, val snapshot: String)
enum class VersionType {
    @SerializedName("snapshot")
    SNAPSHOT,

    @SerializedName("release")
    RELEASE,

    @SerializedName("old_alpha")
    OLD_ALPHA,

}

data class VersionData(
    val id: String,
    val type: VersionType,
    val url: String,
    val releaseTime: String,
    val time: String
)

data class VersionManifest(val latest: LatestData, val versions: MutableList<VersionData>)