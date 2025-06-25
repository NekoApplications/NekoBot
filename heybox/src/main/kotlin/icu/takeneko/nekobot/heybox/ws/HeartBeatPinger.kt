package icu.takeneko.nekobot.heybox.ws

import icu.takeneko.nekobot.CoreEnvironment
import icu.takeneko.nekobot.util.getValue
import icu.takeneko.nekobot.util.setValue
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class HeartBeatPinger(host: HeartBeatHost) : HeartBeatHost by host {
    private var shouldKeepRunning = true

    @OptIn(ExperimentalAtomicApi::class)
    private var counter by AtomicInt(0)

    @OptIn(ObsoleteCoroutinesApi::class)
    private val ticker = flow {
        delay(30 * 1000)
        while (shouldKeepRunning) {
            emit(Unit)
            delay(30 * 1000)
        }
    }

    fun start() {
        ticker.onEach {
            tick()
        }.launchIn(CoreEnvironment.coroutineScope)
    }

    private fun tick() {
        this.beat()
        counter += 1
        if (counter > 3) {
            this.cancel()
            this.onFail()
        }
    }

    fun cancel() {
        shouldKeepRunning = false
    }

    fun pong() {
        counter = 0
    }
}