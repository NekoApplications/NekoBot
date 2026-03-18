package icu.takeneko.nekobot

object TelegramMarkdownSanitizer {
    val replacementRules = mapOf(
        "." to "\\.",
        "<" to "\\<",
        ">" to "\\>",
        "**" to "*",
        "_" to "\\_",
        "|" to "\\|",
        "[" to "\\[",
        "]" to "\\]",
        "(" to "\\(",
        ")" to "\\)",
        "-" to "\\-",
        "+" to "\\+",
    )

    fun accept(input: String): String {
        var result = input
        replacementRules.forEach { a, b ->
            result = result.replace(a, b)
        }
        return result
    }
}