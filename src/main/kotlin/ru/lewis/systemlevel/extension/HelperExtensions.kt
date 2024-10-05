package ru.lewis.systemlevel.extension

import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import me.lucko.helper.event.SingleSubscription
import me.lucko.helper.event.filter.EventFilters
import me.lucko.helper.event.filter.EventHandlers
import me.lucko.helper.event.functional.single.SingleSubscriptionBuilder
import me.lucko.helper.promise.Promise
import me.lucko.helper.scheduler.Scheduler
import me.lucko.helper.scheduler.Task
import me.lucko.helper.scheduler.builder.ContextualTaskBuilder
import me.lucko.helper.scheduler.builder.TaskBuilder
import me.lucko.helper.serialize.BlockPosition
import me.lucko.helper.serialize.BlockRegion
import me.lucko.helper.serialize.ChunkPosition
import me.lucko.helper.terminable.Terminable
import org.bukkit.Bukkit
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.util.BoundingBox
import java.time.Duration
import kotlin.time.toKotlinDuration

inline fun <reified E : Event> subscribe(priority: EventPriority = EventPriority.NORMAL): SingleSubscriptionBuilder<E> {
    return Events.subscribe(E::class.java, priority)
}

fun BlockPosition.withWorld(world: String): BlockPosition {
    if (this.world == world) return this
    return BlockPosition.of(x, y, z, world)
}

fun TaskBuilder.ThreadContextual.every(duration: Duration): ContextualTaskBuilder {
    return this.every(duration.toKotlinDuration())
}

fun TaskBuilder.ThreadContextual.every(duration: kotlin.time.Duration): ContextualTaskBuilder {
    return this.every(duration.inTicks)
}

fun TaskBuilder.ThreadContextual.after(duration: kotlin.time.Duration): TaskBuilder.DelayedTick {
    return this.after(duration.inTicks)
}

fun <T> runAsync(task: () -> T): Promise<T> {
    if (!Bukkit.isStopping() && Bukkit.isPrimaryThread()) {
        return Schedulers.async().call(task)
    }

    return Promise.completed(task())
}

fun <T> runSync(task: () -> T): Promise<T> {
    if (Bukkit.isPrimaryThread()) {
        return Promise.completed(task())
    }

    return Schedulers.sync().call(task)
}

fun <T : Any> T.toTerminable(
    closeFunction: T.() -> Unit,
    isClosedFunction: T.() -> Boolean = { false }
): Terminable {
    return object : Terminable {
        override fun close() = closeFunction()
        override fun isClosed() = isClosedFunction()
    }
}

fun <T : AutoCloseable> T.toTerminable(
    closeFunction: T.() -> Unit = { this.close() },
    isClosedFunction: T.() -> Boolean
): Terminable {
    return object : Terminable {
        override fun close() = closeFunction()
        override fun isClosed() = isClosedFunction()
    }
}

fun <T> Promise<T>.supplyIfPossible(value: T) {
    try {
        this.supply(value)
    } catch (e: IllegalStateException) {
        if (e.message == "Promise is already being supplied.") return
        throw e
    }
}

fun BlockRegion.toBoundingBox(): BoundingBox = BoundingBox.of(
    this.min.toLocation(),
    this.max.toLocation(),
)

fun BlockRegion.chunks(): Sequence<ChunkPosition> {
    val world = this@chunks.min.world
    val minChunk = this.min.toChunk()
    val maxChunk = this.max.toChunk()

    return sequence {
        for (x in minChunk.x..maxChunk.x) {
            for (z in minChunk.z..maxChunk.z) {
                yield(ChunkPosition.of(x, z, world))
            }
        }
    }
}

fun BlockRegion.withWorld(world: String): BlockRegion {
    return BlockRegion.of(min.withWorld(world), max.withWorld(world))
}


fun BlockRegion.blocks(): Sequence<BlockPosition> {
    return (this.min.x..this.max.x).asSequence().map { x ->
        (this.min.z..this.max.z).asSequence().map { z ->
            (this.min.y..this.max.y).asSequence().map { y ->
                BlockPosition.of(x, y, z, this.min.world)
            }
        }
    }.flatMap { it }.flatMap { it }
}

fun BlockRegion.size(): Int {
    return this.width * this.depth * this.height
}

fun Scheduler.runRepeating(
    delay: kotlin.time.Duration = kotlin.time.Duration.ZERO,
    period: kotlin.time.Duration,
    action: (Task) -> Unit
): Task {
    return this.runRepeating(
        { task -> action(task) },
        delay.inTicks,
        period.inTicks,
    )
}

fun <T : Event> SingleSubscriptionBuilder<T>.handlerAsync(action: (T) -> Unit): SingleSubscription<T> {
    return this.handler { Schedulers.async().run { action(it) } }
}

fun <T> SingleSubscriptionBuilder<T>.ignoreCancelled(): SingleSubscriptionBuilder<T> where T : Event, T : Cancellable {
    return this.filter(EventFilters.ignoreCancelled())
}

fun <T> SingleSubscriptionBuilder<T>.cancel(): SingleSubscription<T> where T : Event, T : Cancellable {
    return this.handler(EventHandlers.cancel())
}

inline fun Terminable.whenOpen(action: Terminable.() -> Unit) {
    if (!this.isClosed) action.invoke(this)
}

