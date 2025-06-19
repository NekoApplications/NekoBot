package icu.takeneko.nekobot.heybox

import icu.takeneko.nekobot.NekoBotHost
import icu.takeneko.nekobot.message.builder.MessageCreator
import net.cjsah.bot.api.CardBuilder

fun MessageCreator.heybox(roomId: String, channelId: String, host: NekoBotHost): CardBuilder {
    val cardBuilder = CardBuilder(roomId, channelId)
    throw Exception()
}