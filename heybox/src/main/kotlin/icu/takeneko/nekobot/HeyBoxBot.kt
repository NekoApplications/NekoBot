package icu.takeneko.nekobot

import java.util.concurrent.locks.LockSupport

fun main() {
    val bot = NekoBot()
    bot.bootstrap()
    while (true) {
        LockSupport.parkNanos(100)
    }
}