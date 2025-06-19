package icu.takeneko.nekobot.message.builder

import icu.takeneko.nekobot.message.CommandContext
import io.ktor.http.Url
import java.nio.file.Path

class PageBuilder {
    val elements = mutableListOf<MessageElement>()

    operator fun String.unaryPlus() {
        elements.add(TextElement(this))
    }

    fun text(string: String) {
        elements.add(TextElement(string))
    }

    fun append(string: String) {
        when (val last = elements.last()) {
            is TextElement -> {
                last.text += string
            }

            else -> {
                elements.add(TextElement(string))
            }
        }
    }

    fun drop(): Nothing {
        throw PageDroppedException()
    }

    fun newParagraph() {
        elements.add(EndParagraphElement)
    }

    fun image(path: Path) {
        elements.add(LocalImageElement(path))
    }

    fun image(url: Url) {
        elements.add(RemoteImageElement(url))
    }
}

class MessageCreator(val context: CommandContext) {
    val builders: MutableList<PageBuilder> = mutableListOf()
    val paged: Boolean
        get() = builders.size > 1

    constructor(
        context: CommandContext,
        fn: MessageCreator.() -> Unit
    ) : this(context) {
        this.fn()
    }

    inline fun page(fn: PageBuilder.() -> Unit) {
        val page = try {
            PageBuilder().apply(fn)
        } catch (_: PageDroppedException) {
            return
        }
        builders.add(page)
    }

    fun ensureOnePageExists() {
        if (builders.isEmpty()) {
            builders.add(PageBuilder())
        }
    }

    operator fun String.unaryPlus() {
        ensureOnePageExists()
        builders.last().text(this)
    }

    fun append(string: String) {
        ensureOnePageExists()
        builders.last().append(string)
    }

    operator fun invoke(fn: MessageCreator.() -> Unit): MessageCreator {
        fn(this)
        return this
    }
}

internal class PageDroppedException() : RuntimeException()