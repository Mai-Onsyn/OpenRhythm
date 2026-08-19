package mai_onsyn.open_rhythm.core.midi.device

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import mai_onsyn.open_rhythm.core.GlobalKeyEventDispatcher
import mai_onsyn.open_rhythm.core.midi.MidiEvent
import mai_onsyn.open_rhythm.core.midi.NoteEvent

class KeyboardVirtualMidiInputDevice(
    private val keyInput: GlobalKeyEventDispatcher,
    private var mappings: Map<Long, Int>
) : MidiInputDevice {
    private val eventChannel = Channel<MidiEvent>(128, BufferOverflow.DROP_OLDEST)

    var targetChannel = 0

    private val handler: suspend (KeyEvent) -> Boolean = { keyEvent ->
        if (mappings.containsKey(keyEvent.key.keyCode)) {
            val midiKey = mappings[keyEvent.key.keyCode]!!
            if (keyEvent.type == KeyEventType.KeyDown) {
                eventChannel.send(NoteEvent.noteOn(0, midiKey, 100, targetChannel))
            } else {
                eventChannel.send(NoteEvent.noteOff(0, midiKey, 0, targetChannel))
            }
            false
        } else false
    }
    init {
        keyInput.registerHandler(handler)
    }

    fun updateMappings(newMappings: Map<Long, Int>) {
        this.mappings = newMappings
    }

    override suspend fun clearEvents() {
        while (true) {
            val result = eventChannel.tryReceive()
            if (result.isClosed || result.isFailure) break
        }
    }

    override suspend fun close() {
        keyInput.unregisterHandler(handler)
        eventChannel.close()
    }

    override suspend fun handle(handler: (MidiEvent) -> Unit) {
        handler(eventChannel.receive())
    }
}