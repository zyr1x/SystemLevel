package ru.lewis.systemlevel.model

import com.google.common.base.Throwables
import me.lucko.helper.Schedulers
import me.lucko.helper.terminable.Terminable
import me.lucko.helper.terminable.composite.CompositeClosingException
import me.lucko.helper.terminable.composite.CompositeTerminable
import org.slf4j.Logger
import ru.lewis.systemlevel.extension.inTicks
import ru.lewis.systemlevel.extension.lock
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayDeque
import kotlin.reflect.KClass
import kotlin.time.Duration

class SmartLifoCompositeTerminable(
    private val logger: Logger,
    private val cleanupPeriod: Duration? = null
) : CompositeTerminable {

    private val closeables = ArrayDeque<RegisteredTerminable>(1000)
    private val lock: Lock = ReentrantLock()
    private var closed = false

    fun setup() {
        if (cleanupPeriod != null) {
            Schedulers.builder().async()
                .every(cleanupPeriod.inTicks)
                .run { cleanup() }
                .bindWith(this)
        }
    }

    override fun with(closeable: AutoCloseable?): CompositeTerminable {
        checkNotNull(closeable)

        lock.lock {
            if (closed) {
                logger.warn("Someone is trying to register terminable to the closed composite")
                return this
            }

            val normalizedCloseable = if (closeable is Terminable) closeable else CloseableWrapper(closeable)
            closeables.addLast(RegisteredTerminable(normalizedCloseable, Throwables.getStackTraceAsString(Throwable())))
        }

        return this
    }

    @Throws(CompositeClosingException::class)
    override fun close() {
        doClose().takeIf { it.isNotEmpty() }?.run {
            throw CompositeClosingException(this.map { it.exception })
        }
    }

    override fun closeAndReportException() {
        val errors = doClose()

        val lines = errors.flatMap {
            listOf(
                "",
                "-".repeat(90),
                "***** Component class: ${it.terminable.originalClass()}",
                "Error:",
                Throwables.getStackTraceAsString(it.exception),
                "***** Creation stacktrace:",
                it.terminable.creationStackTrace,
                "-".repeat(90),
                "",
            )
        }.toMutableList()

        if (lines.isNotEmpty()) {
            lines.add(0, "Errors caught while closing composite!")
            logger.error(lines.joinToString(separator = System.lineSeparator()))
        }
    }

    override fun cleanup() {
        lock.lock {
            closeables.removeIf { it.terminable.isClosed }
            closeables.asSequence().mapNotNull { it.terminable as? CompositeTerminable }.forEach { it.cleanup() }
        }
    }

    override fun isClosed(): Boolean {
        return closed
    }

    private fun doClose(): List<CaughtError> {
        lock.lock {
            require(!closed) { "This composite is already closed " }
            closed = true

            val caughtErrors = mutableListOf<CaughtError>()

            while (true) {
                val closeable = closeables.removeLastOrNull() ?: break

                try {
                    if (!closeable.terminable.isClosed) {
                        closeable.terminable.close()
                    }
                } catch (throwable: Throwable) {
                    caughtErrors += CaughtError(throwable, closeable)
                }
            }

            return caughtErrors
        }
    }

    private inner class CloseableWrapper(
        val wrapped: AutoCloseable
    ) : Terminable {

        private var closed = false

        override fun close() {
            wrapped.close()
            closed = true
        }

        override fun isClosed() = closed
    }

    private data class RegisteredTerminable(
        val terminable: Terminable,
        val creationStackTrace: String
    ) {

        fun originalClass(): KClass<out AutoCloseable> {
            if (terminable is CloseableWrapper) {
                return terminable.wrapped::class
            }

            return terminable::class
        }
    }

    private data class CaughtError(val exception: Throwable, val terminable: RegisteredTerminable)
}
