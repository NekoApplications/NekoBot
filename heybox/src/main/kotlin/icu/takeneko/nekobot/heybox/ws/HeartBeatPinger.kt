package icu.takeneko.nekobot.heybox.ws

import icu.takeneko.nekobot.CoreEnvironment
import icu.takeneko.nekobot.util.getValue
import icu.takeneko.nekobot.util.setValue
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class HeartBeatPinger(host: HeartBeatHost) : HeartBeatHost by host {
    private var shouldKeepRunning = true
    private val logger = LoggerFactory.getLogger("HeartBeatPinger")

    @OptIn(ExperimentalAtomicApi::class)
    private var counter by AtomicInteger(0)

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
        logger.debug("Heart beat ping")
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