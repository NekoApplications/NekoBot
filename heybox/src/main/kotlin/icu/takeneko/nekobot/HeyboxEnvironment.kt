package icu.takeneko.nekobot

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus

object HeyboxEnvironment {
    var shouldKeepRunning = true
    val coroutineScope = MainScope() + Dispatchers.IO.limitedParallelism(Runtime.getRuntime().availableProcessors())
    lateinit var mainThread: Thread

    fun destroy() {
        coroutineScope.cancel()
        shouldKeepRunning = false
    }
}