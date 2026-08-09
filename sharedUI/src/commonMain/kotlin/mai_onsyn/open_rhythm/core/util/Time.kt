package mai_onsyn.open_rhythm.core.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Instant

object Time {
    private val timeFormat = LocalDateTime.Format {
        date(LocalDate.Formats.ISO)
        char(' ')
        time(LocalTime.Format {
            hour()
            char(':')
            minute()
            char(':')
            second()
        })
    }

    val nanos: Long get() {
        val instant = Clock.System.now()
        return instant.epochSeconds * 1_000_000_000 + instant.nanosecondsOfSecond
    }
    val micros: Long get() = nanos / 1_000
    val millis: Long get() = Clock.System.now().toEpochMilliseconds()
    val seconds: Long get() = Clock.System.now().epochSeconds

    suspend fun waitMillis(targetDelayMs: Long, onInterrupted: () -> Unit = {}) {
        if (targetDelayMs <= 0) return
        try {
            delay(targetDelayMs.milliseconds)
        } catch (e: CancellationException) {
            onInterrupted()
        }
    }

    suspend fun waitNanos(targetDelayNanos: Long, onInterrupted: () -> Unit = {}) {
        if (targetDelayNanos <= 0) return
        try {
            delay(targetDelayNanos.nanoseconds)
        } catch (e: CancellationException) {
            onInterrupted()
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

    fun formatMillis(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val zone = TimeZone.currentSystemDefault()

        val localDateTime = instant.toLocalDateTime(zone)

        return timeFormat.format(localDateTime)
    }
}

fun Long.pad(width: Int): String {
    return toString().padStart(width, '0')
}