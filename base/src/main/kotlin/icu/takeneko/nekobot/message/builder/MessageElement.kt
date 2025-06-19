package icu.takeneko.nekobot.message.builder

import io.ktor.http.Url
import java.nio.file.Path

interface MessageElement

data class TextElement(var text: String) : MessageElement

object EndParagraphElement: MessageElement

data class LocalImageElement(val path: Path): MessageElement

data class RemoteImageElement(val url: Url): MessageElement