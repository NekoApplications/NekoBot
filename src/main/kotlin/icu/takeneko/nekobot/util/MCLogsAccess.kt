package icu.takeneko.nekobot.util

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.nio.charset.StandardCharsets
import kotlin.jvm.optionals.getOrNull

object MCLogsAccess {
    private val httpClient = HttpClient.newHttpClient()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun getLogContentById(id: String): String {
        val request = HttpRequest.newBuilder().GET().uri(URI("https://api.mclo.gs/1/raw/$id"))
        val response = httpClient.send(request.build(), BodyHandlers.ofString())

        if ("application/json" == response.headers().firstValue("Content-Type").getOrNull()
            || response.statusCode() != 200
        ) {
            throw IllegalArgumentException("Log ($id) not found.")
        }
        return response.body()
    }

    fun updateLogContent(content: String): String {
        val encoded = "content=${URLEncoder.encode(content, StandardCharsets.UTF_8)}"
        val request = HttpRequest.newBuilder().POST(
            BodyPublishers.ofString(
                encoded
            )
        ).uri(URI("https://api.mclo.gs/1/log")).header("Content-Type", "application/x-www-form-urlencoded")
        val response = httpClient.send(request.build(), BodyHandlers.ofString())
        val result = json.decodeFromString<LogPasteResult>(response.body())
        if (!result.success) {
            throw IllegalArgumentException(result.error)
        }
        return result.id
    }

    @kotlinx.serialization.Serializable
    data class LogPasteResult(
        val success: Boolean,
        val error: String = "",
        val url: String = "",
        val id: String = "",
        val raw: String = "",
    )
}