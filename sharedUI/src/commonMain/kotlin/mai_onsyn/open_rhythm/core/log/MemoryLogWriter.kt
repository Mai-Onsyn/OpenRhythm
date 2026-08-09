package mai_onsyn.open_rhythm.core.log

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import mai_onsyn.open_rhythm.core.util.Time
import kotlin.collections.toList

@OptIn(InternalCoroutinesApi::class)
class MemoryLogWriter(
    var maxSize: Int = 1000
) : LogWriter() {

    private val lock = SynchronizedObject()
    private val logs = ArrayDeque<LogEntry>()

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        synchronized(lock) {
            if (logs.size >= maxSize) {
                logs.removeFirst()
            }
            logs.addLast(LogEntry(
                severity = severity,
                tag = tag,
                message = message,
                throwable = throwable
            ))
        }
    }

    fun forEach(action: (LogEntry) -> Unit) {
        synchronized(lock) {
            logs.forEach(action)
        }
    }
    fun getAllLogs(): List<LogEntry> = synchronized(lock) { logs.toList() }
    fun clear() = synchronized(lock) { logs.clear() }
}

data class LogEntry(
    val timestamp: Long = Time.millis,
    val severity: Severity,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null
)