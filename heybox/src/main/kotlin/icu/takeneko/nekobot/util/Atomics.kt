package icu.takeneko.nekobot.util

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KProperty

@OptIn(ExperimentalAtomicApi::class)
operator fun AtomicInt.getValue(pinger: Any?, prop: KProperty<*>): Int {
    return this.load()
}

@OptIn(ExperimentalAtomicApi::class)
operator fun AtomicInt.setValue(pinger: Any?, prop: KProperty<*>, value: Int) {
    this.store(value)
}