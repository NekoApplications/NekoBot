package icu.takeneko.nekobot.heybox.event

import icu.takeneko.nekobot.CoreEnvironment.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.cjsah.bot.event.Event
import org.slf4j.LoggerFactory

object EventDispatcher {
    private val logger = LoggerFactory.getLogger("EventDispatcher")

    private val eventSubscribers: MutableMap<Class<out Event>, MutableMap<EventPriority, MutableList<suspend Event.() -> Unit>>> = mutableMapOf()

    fun getSubscribers(clazz: Class<out Event>): MutableMap<EventPriority, MutableList<suspend Event.() -> Unit>> {
        return eventSubscribers.computeIfAbsent(clazz) { mutableMapOf() }
    }

    fun getSubscribers(clazz: Class<out Event>, priority: EventPriority): MutableList<suspend Event.() -> Unit> {
        return getSubscribers(clazz).computeIfAbsent(priority) { mutableListOf() }
    }

    inline fun <reified T : Event> subscribe(
        priority: EventPriority = EventPriority.NORMAL,
        noinline fn: suspend T.() -> Unit
    ) {
        getSubscribers(T::class.java, priority).add(fn as suspend Event.() -> Unit)
    }

    fun dispatch(e: Event) {
        coroutineScope.launch(Dispatchers.IO) {
            dispatchSuspend(e)
        }
    }

    suspend fun dispatchSuspend(e: Event) {
        for (priority in EventPriority.entries) {
            for (function in getSubscribers(e.javaClass, priority)) {
                try {
                    e.function()
                } catch (t: Throwable) {
                    logger.error("An exception was thrown while posing event {} to {}.", e, function, t)
                }
            }
        }
    }
}