package mai_onsyn.open_rhythm.core.midi

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AtomicReference
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.core.util.msAtTick
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

data class UIMidiData(
    val fileName: String,
    val path: String,
    val duration: Double,
    val pianoOnly: Boolean,
    val trackCount: Int,
    val noteCount: Int
)

@OptIn(ExperimentalAtomicApi::class)
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

    suspend fun loadFile(path: String): Midi = load(
        path, cachedFileInfos, loadingFileDeferred
    ) { _loadFile(path) }

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

    var loadedFileCount by mutableStateOf(0)
    var remandingFileCount by mutableStateOf(0)
    private suspend fun _loadFolder(path: String): List<UIMidiData> = withContext(Dispatchers.IO) {
        val parentFolder = PlatformFile(path)
        if (!parentFolder.exists() && !parentFolder.isDirectory()) {
            return@withContext emptyList()
        }

        mtx.withLock {
            remandingFileCount = 0
            loadedFileCount = 0
        }
        val allFiles = parentFolder.list()
        if (allFiles.isEmpty()) return@withContext emptyList()

        val midFiles = coroutineScope {
            allFiles.map { file ->
                async(Dispatchers.IO) { // isRegularFile在安卓很耗时
                    if (file.isRegularFile() && file.extension == "mid") file else null
                }
            }.mapNotNull { it.await() }
        }
        if (midFiles.isEmpty()) return@withContext emptyList()
        mtx.withLock {
            remandingFileCount = midFiles.size
        }

        coroutineScope {
            midFiles.map { file ->
                async {     // 并行优化加载
                    try {
                        val midi = _loadFile(file)
                        var pianoOnly = true
                        for (track in midi.tracks) {
                            if (track.trackInst != 0) {
                                pianoOnly = false
                                break
                            }
                        }
                        UIMidiData(
                            fileName = file.nameWithoutExtension,
                            path = file.absolutePath(),
                            duration = midi.msAtTick(midi.totalTicks.toLong()),
                            pianoOnly = pianoOnly,
                            trackCount = midi.tracks.size,
                            midi.totalNotes
                        ).also { mtx.withLock {
                            loadedFileCount++
                        } }
                    } catch (e: Exception) {
                        Logger.w(e) { "Failed to load midi file: ${file.name}" }
                        null
                    }
                }
            }.mapNotNull { it.await() }
        }
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
            if (Global.settings.UseParserV1) parseMidiV1(name, bytes)
            else parseMidi(name, bytes, file.absolutePath()).apply { if (Global.settings.SortTracksByPitch) sortTracksByPitches() }
        }
        return midi
    }
}