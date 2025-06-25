package icu.takeneko.nekobot.util

import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KProperty

@OptIn(ExperimentalAtomicApi::class)
operator fun AtomicInteger.getValue(pinger: Any?, prop: KProperty<*>): Int {
    return this.get()
}

@OptIn(ExperimentalAtomicApi::class)
operator fun AtomicInteger.setValue(pinger: Any?, prop: KProperty<*>, value: Int) {
    this.set(value)
}