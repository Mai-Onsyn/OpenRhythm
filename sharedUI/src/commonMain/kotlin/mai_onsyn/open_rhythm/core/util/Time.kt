package mai_onsyn.open_rhythm.core.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.time.Clock
import kotlin.time.TimeSource

object Time {
    val nanos: Long get() {
        val instant = Clock.System.now()
        return instant.epochSeconds * 1_000_000_000 + instant.nanosecondsOfSecond
    }
    val micros: Long get() = nanos / 1_000
    val millis: Long get() = Clock.System.now().toEpochMilliseconds()
    val seconds: Long get() = Clock.System.now().epochSeconds

    suspend fun wait(targetDelayMs: Long, onInterrupted: () -> Unit = {}) {
        val timer = TimeSource.Monotonic
        val start = timer.markNow()

        if (targetDelayMs > 2) {
            val safeDelayMs = (targetDelayMs - 2).coerceAtLeast(0)
            try {
                delay(safeDelayMs)
            } catch (e: CancellationException) {
                onInterrupted()
                throw e
            }
        }

        while (true) {
            if (!currentCoroutineContext().isActive) {
                onInterrupted()
                break
            }
            val elapsed = start.elapsedNow().inWholeNanoseconds
            val targetNs = targetDelayMs * 1_000_000L
            if (elapsed >= targetNs) break

            if (targetNs - elapsed > 100_000) {
                yield()
            }
        }
    }
}