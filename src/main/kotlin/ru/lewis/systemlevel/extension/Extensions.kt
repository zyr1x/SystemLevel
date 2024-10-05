package ru.lewis.systemlevel.extension

import io.github.blackbaroness.durationserializer.DurationFormats
import io.github.blackbaroness.durationserializer.DurationSerializer
import io.github.blackbaroness.durationserializer.format.DurationFormat
import net.kyori.adventure.util.Ticks
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.locks.Lock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.toKotlinDuration

@OptIn(ExperimentalContracts::class)
inline fun <T> Lock.lock(action: () -> T): T {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }

    this.lock()
    try {
        return action()
    } finally {
        this.unlock()
    }
}

fun randomNumber(min: Double, max: Double): Double {
    return Math.random() * (max - min) + min
}

// Extension function to add a percentage to a Double
fun Double.addPercent(percent: Double): Double {
    return this + (this * percent / 100)
}

// Extension function to remove a percentage from a Double
fun Double.removePercent(percent: Double): Double {
    return this - (this * percent / 100)
}

fun Duration.format(
    unit: ChronoUnit = ChronoUnit.MINUTES,
    format: DurationFormat = DurationFormats.mediumLengthRussian(),
    avoidZero: Boolean = true
) = truncate(unit, avoidZero).format(format)

fun Throwable.rootCause(): Throwable {
    var cause = this.cause ?: return this

    while (true) {
        cause = cause.cause ?: return cause
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> Iterable<Lock>.lock(action: () -> T): T {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }

    val lockedLocks = mutableListOf<Lock>()

    try {
        this.forEach { it.lock(); lockedLocks += it }
        return action.invoke()
    } finally {
        lockedLocks.forEach { it.unlock() }
    }
}


fun Duration.truncate(unit: ChronoUnit, avoidZero: Boolean = true): Duration {
    val duration = truncatedTo(unit)
    if (avoidZero && duration < unit.duration) {
        return duration.plus(unit.duration)
    }

    return duration
}

fun Duration.format(durationFormat: DurationFormat): String {
    return DurationSerializer.serialize(this, durationFormat)
}

fun Instant.timeUntil(otherInstant: Instant): kotlin.time.Duration {
    return Duration.between(this, otherInstant).toKotlinDuration()
}

fun Duration.isPositive(): Boolean {
    return !isZero && !isNegative
}

fun kotlin.time.Duration.atLeast(duration: kotlin.time.Duration): kotlin.time.Duration {
    return if (this < duration) duration else this
}

@OptIn(ExperimentalContracts::class)
inline fun <T> Iterable<T>.forEachOther(action: (T, T) -> Unit) {
    contract {
        callsInPlace(action, InvocationKind.UNKNOWN)
    }

    this.forEach { t1 ->
        this.forEach { t2 ->
            action(t1, t2)
        }
    }
}

val kotlin.time.Duration.isZero: Boolean
    get() = this.inWholeMilliseconds == 0L

fun <K, V, R> Map<K, V?>.mapNotNullValues(transform: (K, V) -> R): Map<K, R> {
    return this
        .filterValues { it != null }
        .mapValues { transform(it.key, it.value!!) }
}

val kotlin.time.Duration.inTicks: Long
    get() {
        return this.inWholeMilliseconds / Ticks.SINGLE_TICK_DURATION_MS
    }

