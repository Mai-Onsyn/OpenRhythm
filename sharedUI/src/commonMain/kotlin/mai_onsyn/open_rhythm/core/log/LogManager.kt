package mai_onsyn.open_rhythm.core.log

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.sink
import kotlinx.io.buffered
import kotlinx.io.writeString
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.util.Time

object LogManager {
    private val memoryLogWriter = MemoryLogWriter()

    fun initialize() {
        Logger.setLogWriters(
            platformLogWriter(),
            memoryLogWriter
        )
        Logger.setMinSeverity(Severity.entries[Global.settings.LogLevel])
        memoryLogWriter.maxSize = Global.settings.MaxLogCount
    }

    suspend fun export(
        onCompleted: (PlatformFile, Int) -> Unit = { _, _ -> },
        onFailed: (PlatformFile, Exception) -> Unit = { _, _ -> }
    ) {
        val file = FileKit.openFileSaver(
            suggestedName = "log-${Time.formatMillis(Time.millis)}".replace(":", "-"),
            defaultExtension = "txt",
            allowedExtensions = setOf("txt", "log")
        ) ?: return
        try {
            file.sink().buffered().use { output ->
                if (memoryLogWriter.recordedCount > memoryLogWriter.count) {
                    output.writeString("The number of logs has exceeded the set record limit: originally ${memoryLogWriter.recordedCount}; ${memoryLogWriter.count} were output\n")
                }
                memoryLogWriter.forEach { entry ->
                    output.writeString("[${Time.formatMillis(entry.timestamp)}] [${entry.severity.name}] ${entry.message}\n")
                    entry.throwable?.let { throwable ->
                        output.writeString("${throwable.stackTraceToString()}\n")
                    }
                }
            }
            Logger.i { "Log exported to ${file.absolutePath()}" }
            onCompleted(file, memoryLogWriter.count)
        } catch (e: Exception) {
            Logger.e(e) { "Log export failed" }
            onFailed(file, e)
        }
    }

    fun clearLogs() {
        memoryLogWriter.clear()
    }
}