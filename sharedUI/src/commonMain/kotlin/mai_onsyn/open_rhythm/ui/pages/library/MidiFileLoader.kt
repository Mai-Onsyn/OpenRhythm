package mai_onsyn.open_rhythm.ui.pages.library

import io.github.vinceglb.filekit.*
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.msAtTick

data class UIMidiData(
    val path: String,
    val duration: Double,
    val pianoOnly: Boolean,
    val trackCount: Int
)

val cachedMidiFiles = mutableMapOf<String, List<UIMidiData>>()

suspend fun getFilesInFolder(path: String): List<UIMidiData> {
    if (cachedMidiFiles.contains(path)) return cachedMidiFiles[path]!!

    val result = mutableListOf<UIMidiData>()

    val parentFolder = PlatformFile(path)
    if (!parentFolder.exists() && !parentFolder.isDirectory()) {
        return result
    }

    parentFolder.list().forEach {
        if (it.isRegularFile() && it.extension == "mid") {
            val midi = Midi.fromFile(it.nameWithoutExtension, it.readBytes().toList())

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
                path = it.name,
                duration = midi.msAtTick(midi.totalTicks.toLong()),
                pianoOnly = pianoOnly,
                trackCount = midi.hasNoteTracks
            ))
        }
    }
    cachedMidiFiles[path] = result

    return result
}