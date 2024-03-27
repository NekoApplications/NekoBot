package icu.takeneko.nekobot

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent


object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "icu.takeneko.nekobot",
        name = "NekoBot",
        version = BuildConstants.VERSION
    ) {
        author("ZhuRuoLing")
    }
) {
    override fun onEnable() {
        logger.info("Nya!")
        val eventChannel = GlobalEventChannel.parentScope(this)
        eventChannel.subscribeAlways<GroupMessageEvent> {
//            if (message.contentToString().startsWith("复读")) {
//                group.sendMessage(message.contentToString().replace("复读", ""))
//            }
//            if (message.contentToString() == "hi") {
//                group.sendMessage("hi")
//                sender.sendMessage("hi")
//                return@subscribeAlways
//            }
//            message.forEach {
//                if (it is Image) {
//                    val url = it.queryUrl()
//                    group.sendMessage("图片，下载地址$url")
//                }
//                if (it is PlainText) {
//                    group.sendMessage("纯文本，内容:${it.content}")
//                }
//            }
        }
        eventChannel.subscribeAlways<FriendMessageEvent> {
            if (message.contentToString() == "PING") {
                sender.sendMessage("PONG")
            }
        }
    }
}
