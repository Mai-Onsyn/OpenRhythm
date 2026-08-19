package mai_onsyn.open_rhythm.ui.pages.library

import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.util.msAtTick
import mai_onsyn.open_rhythm.core.midi.parseMidi

data class UIMidiData(
    val fileName: String,
    val path: String,
    val duration: Double,
    val pianoOnly: Boolean,
    val trackCount: Int
)

val cachedMidiFileInfos = mutableMapOf<String, List<UIMidiData>>()

suspend fun getFileInfosInFolder(path: String): List<UIMidiData> {
    if (cachedMidiFileInfos.contains(path)) return cachedMidiFileInfos[path]!!

    val result = mutableListOf<UIMidiData>()

    val parentFolder = PlatformFile(path)
    if (!parentFolder.exists() && !parentFolder.isDirectory()) {
        return result
    }

    parentFolder.list().forEach {
        if (it.isRegularFile() && it.extension == "mid") {
            try {
                val midi = loadMidiFile(it)
                var pianoOnly = true
                for (track in midi.tracks) {
                    val bb = track.instrumentEvent.program == 0
                    if (!bb) {
                        pianoOnly = false
                        break
                    }
                }

                result.add(UIMidiData(
                    fileName = it.nameWithoutExtension,
                    path = it.absolutePath(),
                    duration = midi.msAtTick(midi.totalTicks.toLong()),
                    pianoOnly = pianoOnly,
                    trackCount = midi.tracks.size
                ))
            } catch (e: Exception) {
                Logger.w { "Failed to load midi file: ${it.name}" }
                e.printStackTrace()
                return@forEach
            }
        }
    }
    cachedMidiFileInfos[path] = result

    return result
}

val cachedMidiFiles = mutableMapOf<String, Midi>()
suspend fun loadMidiFile(path: String): Midi? {
    val file = PlatformFile(path)
    return if (file.exists() && file.isRegularFile() && file.extension == "mid") {
        loadMidiFile(file)
    }
    else null
}

suspend fun loadMidiFile(file: PlatformFile): Midi {
    if (cachedMidiFiles.containsKey(file.path)) {
        return cachedMidiFiles[file.path]!!
    }
//    val readStart = Time.nanos
    val bytesArray = file.readBytes()
//    val parseStartReadEnd = Time.nanos
    val midi = withContext(Dispatchers.Default) {
        val bytes = bytesArray.toList()
        if (Global.settings.UseParserV1) Midi.fromFile(file.nameWithoutExtension, bytes)
        else parseMidi(file.nameWithoutExtension, bytes)
    }
//    val parseEnd = Time.nanos
//    Logger.d { "Parse ${file.name}, read: ${(parseStartReadEnd - readStart) / 1000000f}ms; parse: ${(parseEnd - parseStartReadEnd) / 1000000f}ms" }

    var noteCount = 0
    for (track in midi.tracks) {
        noteCount += track.notes.size

        if (noteCount > 100_000) return midi
    }
    if (noteCount > 100_000) return midi
    else cachedMidiFiles[file.path] = midi

    return midi
}