package mai_onsyn.open_rhythm.ui.pages.library

import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.*
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.msAtTick

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
                for (t in 0 until midi.hasNoteTracks) {
                    val track = midi.tracks[t]
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
                    trackCount = midi.hasNoteTracks
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
    val midi = Midi.fromFile(file.nameWithoutExtension, file.readBytes().toList())

    var noteCount = 0
    for (track in midi.tracks) {
        noteCount += track.notes.size

        if (noteCount > 100_000) return midi
    }
    if (noteCount > 100_000) return midi
    else cachedMidiFiles[file.path] = midi

    return midi
}