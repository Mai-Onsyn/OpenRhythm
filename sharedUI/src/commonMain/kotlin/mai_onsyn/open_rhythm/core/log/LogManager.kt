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
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.util.Time

object LogManager {
    private val memoryLogWriter = MemoryLogWriter()

    fun initialize() {
        Logger.setLogWriters(
            platformLogWriter(),
            memoryLogWriter
        )
        Logger.setMinSeverity(Severity.entries[Singleton.settings.LogLevel])
        memoryLogWriter.maxSize = Singleton.settings.MaxLogCount
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
            var exportedCount = 0
            file.sink().buffered().use { output ->
                memoryLogWriter.forEach { entry ->
                    output.writeString("[${Time.formatMillis(entry.timestamp)}] [${entry.severity.name}] ${entry.message}\n")
                    entry.throwable?.let { throwable ->
                        output.writeString("${throwable.stackTraceToString()}\n")
                    }
                    exportedCount++
                }
            }
            Logger.i { "Log exported to ${file.absolutePath()}" }
            onCompleted(file, exportedCount)
        } catch (e: Exception) {
            Logger.e(e) { "Log export failed" }
            onFailed(file, e)
        }
    }

    fun clearLogs() {
        memoryLogWriter.clear()
    }
}