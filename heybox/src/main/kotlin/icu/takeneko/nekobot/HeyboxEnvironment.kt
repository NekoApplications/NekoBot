package icu.takeneko.nekobot

object HeyboxEnvironment {
    var shouldKeepRunning = true
    lateinit var mainThread: Thread

    fun destroy() {
        CoreEnvironment.destroy()
        shouldKeepRunning = false
    }
}