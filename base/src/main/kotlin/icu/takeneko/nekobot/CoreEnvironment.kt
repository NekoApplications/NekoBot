package icu.takeneko.nekobot

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus

object CoreEnvironment {
    var permissionManagementEnabled = true
    val coroutineScope = MainScope() + Dispatchers.IO.limitedParallelism(Runtime.getRuntime().availableProcessors())

    fun destroy() {
        coroutineScope.cancel()
    }
}