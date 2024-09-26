package icu.takeneko.nekobot.i18n

import icu.takeneko.nekobot.message.MessageResponseCreationScope
import icu.takeneko.nekobot.preference.Preference
import icu.takeneko.nekobot.util.BuildProperties

internal val languagePrefKey = "bot.i18n.language"

object I18n {
    val defaultLanguage: String = BuildProperties["default_language"]!!
    val supportedLanguages: List<String> = BuildProperties["supported_languages"]!!.split(",")
    private val data = mutableMapOf<String, Resource>()

    fun init() {
        data += supportedLanguages.map { it to Resource("lang/$it").apply(Resource::read) }
    }

    fun translate(language: String, key: String, vararg formatArgs: Any?): String {
        return this[language][key]?.format(*formatArgs) ?: key
    }

    operator fun get(key: String): Resource {
        return data[key] ?: data[defaultLanguage]!!
    }
}

fun MessageResponseCreationScope.tr(key: String, vararg formatArgs: Any?): String {
    return I18n.translate(
        Preference.get(this.context, languagePrefKey)?: I18n.defaultLanguage,
        key,
        *formatArgs
    )
}