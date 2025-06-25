package icu.takeneko.nekobot.heybox.ws

interface HeartBeatHost {
    fun beat()

    fun onFail()
}