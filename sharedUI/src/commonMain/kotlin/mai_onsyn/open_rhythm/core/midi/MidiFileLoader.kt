package mai_onsyn.open_rhythm.core.midi

import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.util.msAtTick

data class UIMidiData(
    val fileName: String,
    val path: String,
    val duration: Double,
    val pianoOnly: Boolean,
    val trackCount: Int,
    val noteCount: Int
)

class MidiFileLoader {
    private val mtx = Mutex()
    private val cachedFolderContentInfos = mutableMapOf<String, List<UIMidiData>>()
    private val cachedFileInfos = mutableMapOf<String, Midi>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val loadingFolderDeferred = mutableMapOf<String, CompletableDeferred<List<UIMidiData>>>()
    private val loadingFileDeferred = mutableMapOf<String, CompletableDeferred<Midi>>()

    suspend fun loadFolder(path: String): List<UIMidiData> = load(
        path, cachedFolderContentInfos, loadingFolderDeferred
    ) { _loadFolder(path) }

    suspend fun loadFile(fileName: String): Midi = load(
        fileName, cachedFileInfos, loadingFileDeferred
    ) { _loadFile(fileName) }

    fun isFolderLoaded(path: String): Boolean = cachedFolderContentInfos.containsKey(path)

    fun clearCache() {
        cachedFileInfos.clear()
        cachedFolderContentInfos.clear()
    }

    private suspend fun <T> load(
        path: String,
        cache: MutableMap<String, T>,
        loadingDeferreds: MutableMap<String, CompletableDeferred<T>>,
        loader: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        mtx.withLock {
            cache[path]?.let { return@withContext it }
        }

        val deferred = mtx.withLock {
            cache[path]?.let { return@withLock null }
            loadingDeferreds[path]?.let { return@withLock it }  // 已有任务
            val newDeferred = CompletableDeferred<T>()
            loadingDeferreds[path] = newDeferred
            scope.launch {
                try {
                    val result = loader()  // 实际加载
                    mtx.withLock {
                        cache[path] = result
                        loadingDeferreds.remove(path)?.complete(result)
                    }
                } catch (e: Throwable) {
                    mtx.withLock {
                        loadingDeferreds.remove(path)?.completeExceptionally(e)
                    }
                }
            }
            newDeferred
        } ?: return@withContext mtx.withLock { cache[path]!! }  // 缓存被其他协程填充

        return@withContext deferred.await()
    }

    private suspend fun _loadFolder(path: String): List<UIMidiData> = withContext(Dispatchers.IO) {
        val result = mutableListOf<UIMidiData>()

        val parentFolder = PlatformFile(path)
        if (!parentFolder.exists() && !parentFolder.isDirectory()) {
            return@withContext result
        }

        parentFolder.list().forEach {
            if (it.isRegularFile() && it.extension == "mid") {
                try {
                    val midi = _loadFile(it)
                    var pianoOnly = true
                    for (track in midi.tracks) {
                        val bb = track.trackInst == 0
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
                        trackCount = midi.tracks.size,
                        midi.totalNotes
                    ))
                } catch (e: Exception) {
                    Logger.w(e) { "Failed to load midi file: ${it.name}" }
                    return@forEach
                }
            }
        }

        return@withContext result
    }

    private suspend fun _loadFile(path: String): Midi = _loadFile(PlatformFile(path))

    private suspend fun _loadFile(file: PlatformFile): Midi {
        val (bytes, name) = withContext(Dispatchers.IO) {
            if (file.exists() && file.isRegularFile() && file.extension == "mid") {
                file.readBytes() to file.nameWithoutExtension
            } else throw IOException("Can't read ${file.absolutePath()} as a MIDI file")
        }
        val midi = withContext(Dispatchers.Default) {
            val bytes = bytes.toList()
            if (Global.settings.UseParserV1) Midi.fromFile(name, bytes)
            else parseMidi(name, bytes)
        }
        return midi
    }
}

//val cachedMidiFileInfos = mutableMapOf<String, List<UIMidiData>>()
//
//suspend fun getFileInfosInFolder(path: String): List<UIMidiData> {
//    if (cachedMidiFileInfos.contains(path)) return cachedMidiFileInfos[path]!!
//    cachedMidiFileInfos[path] = mutableListOf()
//
//    val result = mutableListOf<UIMidiData>()
//
//    val parentFolder = PlatformFile(path)
//    if (!parentFolder.exists() && !parentFolder.isDirectory()) {
//        return result
//    }
//
//    parentFolder.list().forEach {
//        if (it.isRegularFile() && it.extension == "mid") {
//            try {
//                val midi = loadMidiFile(it)
//                var pianoOnly = true
//                for (track in midi.tracks) {
//                    val bb = track.instrumentEvent.program == 0
//                    if (!bb) {
//                        pianoOnly = false
//                        break
//                    }
//                }
//
//                result.add(UIMidiData(
//                    fileName = it.nameWithoutExtension,
//                    path = it.absolutePath(),
//                    duration = midi.msAtTick(midi.totalTicks.toLong()),
//                    pianoOnly = pianoOnly,
//                    trackCount = midi.tracks.size
//                ))
//            } catch (e: Exception) {
//                Logger.w { "Failed to load midi file: ${it.name}" }
//                e.printStackTrace()
//                return@forEach
//            }
//        }
//    }
//    cachedMidiFileInfos[path] = result
//
//    return result
//}
//
//val cachedMidiFiles = mutableMapOf<String, Midi>()
//suspend fun loadMidiFile(path: String): Midi? {
//    val file = PlatformFile(path)
//    return if (file.exists() && file.isRegularFile() && file.extension == "mid") {
//        loadMidiFile(file)
//    }
//    else null
//}
//
//suspend fun loadMidiFile(file: PlatformFile): Midi {
//    if (cachedMidiFiles.containsKey(file.path)) {
//        return cachedMidiFiles[file.path]!!
//    }
//
//    val bytesArray = file.readBytes()
//    val midi = withContext(Dispatchers.Default) {
//        val bytes = bytesArray.toList()
//        if (Global.settings.UseParserV1) Midi.fromFile(file.nameWithoutExtension, bytes)
//        else parseMidi(file.nameWithoutExtension, bytes)
//    }
//    var noteCount = 0
//    for (track in midi.tracks) {
//        noteCount += track.notes.size
//
//        if (noteCount > 100_000) return midi
//    }
//    if (noteCount > 100_000) return midi
//    else cachedMidiFiles[file.path] = midi
//
//    return midi
//}