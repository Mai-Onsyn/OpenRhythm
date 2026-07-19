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

    fun formatMillisToTime(millis: Float): String {
        return formatMillisToTime(millis.toLong())
    }

    fun formatMillisToTime(millis: Int): String {
        return formatMillisToTime(millis.toLong())
    }

    fun formatMillisToTime(millis: Double): String {
        return formatMillisToTime(millis.toLong())
    }

    fun formatMillisToTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) "${hours.pad(2)}:${minutes.pad(2)}:${seconds.pad(2)}"//String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else "${minutes.pad(2)}:${seconds.pad(2)}"//String.format("%02d:%02d", minutes, seconds)
    }
}

fun Long.pad(width: Int): String {
    return toString().padStart(width, '0')
}