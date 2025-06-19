package icu.takeneko.nekobot

import icu.takeneko.nekobot.message.builder.LocalImageElement
import icu.takeneko.nekobot.message.builder.RemoteImageElement

interface NekoBotHost {
    fun uploadImage(image: LocalImageElement): RemoteImageElement
}