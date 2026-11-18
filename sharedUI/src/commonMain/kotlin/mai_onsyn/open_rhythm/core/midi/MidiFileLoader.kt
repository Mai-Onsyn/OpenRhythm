package mai_onsyn.open_rhythm.core.midi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.util.msAtTick
import kotlin.concurrent.atomics.ExperimentalAtomicApi

data class UIMidiData(
    val fileName: String,
    val path: String,
    val duration: Double,
    val pianoOnly: Boolean,
    val trackCount: Int,
    val noteCount: Int,
    val fileSize: Long,
    val loaded: Boolean
)

@OptIn(ExperimentalAtomicApi::class)
class MidiFileLoader {
    private val mtx = Mutex()
    private val cachedFolderContentInfos = mutableMapOf<String, List<UIMidiData>>()
    private val cachedFileInfos = mutableMapOf<String, Midi>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val loadingFolderDeferred = mutableMapOf<String, CompletableDeferred<List<UIMidiData>>>()
    private val loadingFileDeferred = mutableMapOf<String, CompletableDeferred<Midi>>()

    fun fileIsLoading(path: String): Boolean = loadingFileDeferred.containsKey(path)

    suspend fun loadFolder(path: String): List<UIMidiData> = //_loadFolder(path)
        load(
        path, cachedFolderContentInfos, loadingFolderDeferred
    ) { _loadFolder(path) }

    suspend fun loadFile(path: String, onLoaded: (Midi) -> Unit = {}): Midi = load(
        path, cachedFileInfos, loadingFileDeferred
    ) { _loadFile(path, onLoaded) }

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
                        val fileSize = file.size()
                        val filePath = file.absolutePath()
                        val shouldPlaceholder = fileSize > Global.settings.BlackMIDISizeThreshold && mtx.withLock {
                            !cachedFileInfos.containsKey(filePath) &&
                                    !loadingFileDeferred.containsKey(filePath)
                        }
                        if (shouldPlaceholder) {
                            return@async UIMidiData(
                                fileName = file.nameWithoutExtension,
                                path = filePath,
                                duration = 0.0,
                                pianoOnly = false,
                                trackCount = 0,
                                noteCount = 0,
                                fileSize = fileSize,
                                loaded = false
                            ).also { mtx.withLock { loadedFileCount++ } }
                        }

                        val midi = loadFile(filePath)
                        val pianoOnly = midi.tracks.all { it.trackInst == 0 }
                        UIMidiData(
                            fileName = file.nameWithoutExtension,
                            path = filePath,
                            duration = midi.msAtTick(midi.totalTicks.toLong()),
                            pianoOnly = pianoOnly,
                            trackCount = midi.tracks.size,
                            noteCount = midi.totalNotes,
                            fileSize = fileSize,
                            loaded = true
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

    private suspend fun _loadFile(path: String, onLoaded: (Midi) -> Unit = {}): Midi = _loadFile(PlatformFile(path), onLoaded)

    private suspend fun _loadFile(file: PlatformFile, onLoaded: (Midi) -> Unit = {}): Midi {
        val (bytes, name) = withContext(Dispatchers.IO) {
            if (file.exists() && file.isRegularFile() && file.extension == "mid") {
                file.readBytes() to file.nameWithoutExtension
            } else throw IOException("Can't read ${file.absolutePath()} as a MIDI file")
        }
        val midi = withContext(Dispatchers.Default) {
            val bytes = bytes.asList()
            if (Global.settings.UseParserV1) parseMidiV1(name, bytes)
            else parseMidi(name, bytes, file.absolutePath()).apply { if (Global.settings.SortTracksByPitch) sortTracksByPitches() }
        }
        upgradeFolderCache(file.absolutePath(), midi)
        onLoaded(midi)
        return midi
    }

    private suspend fun upgradeFolderCache(path: String, midi: Midi) {
        mtx.withLock {
            for ((folderPath, list) in cachedFolderContentInfos) {
                val idx = list.indexOfFirst { it.path == path }
                if (idx < 0) continue
                val old = list[idx]
                if (old.loaded) continue
                val upgraded = UIMidiData(
                    fileName = old.fileName,
                    path = path,
                    duration = midi.msAtTick(midi.totalTicks.toLong()),
                    pianoOnly = midi.tracks.all { it.trackInst == 0 },
                    trackCount = midi.tracks.size,
                    noteCount = midi.totalNotes,
                    fileSize = old.fileSize,
                    loaded = true
                )
                cachedFolderContentInfos[folderPath] = list.toMutableList().also { it[idx] = upgraded }
            }
        }
    }
}