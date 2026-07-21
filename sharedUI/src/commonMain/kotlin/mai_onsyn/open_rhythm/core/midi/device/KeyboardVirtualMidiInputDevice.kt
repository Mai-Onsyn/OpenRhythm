package mai_onsyn.open_rhythm.core.midi.device

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import mai_onsyn.open_rhythm.core.GlobalKeyEventDispatcher
import mai_onsyn.open_rhythm.core.midi.MidiEvent
import mai_onsyn.open_rhythm.core.midi.NoteEvent

class KeyboardVirtualMidiInputDevice(
    keyInput: GlobalKeyEventDispatcher
) : MidiInputDevice {
    private val eventChannel = Channel<MidiEvent>(128, BufferOverflow.DROP_OLDEST)
    private val userKeys = arrayOf(Key.A, Key.W, Key.S, Key.E, Key.D, Key.F, Key.T, Key.G, Key.Y, Key.H, Key.U, Key.J, Key.K, Key.O, Key.L, Key.P, Key.Semicolon, Key.Apostrophe)
    init {
        keyInput.registerHandler { keyEvent ->
//            Logger.d { "KeyboardMidiInputDevice: received keyEvent $keyEvent" }
            val idx = userKeys.indexOf(keyEvent.key)
            if (idx != -1) {
                val midiKey = idx + 60
                if (keyEvent.type == KeyEventType.KeyDown) {
                    eventChannel.send(NoteEvent.noteOn(0, midiKey, 100, 0))
                } else {
                    eventChannel.send(NoteEvent.noteOff(0, midiKey, 0, 0))
                }
                return@registerHandler true
            }

            false
        }
    }

    override suspend fun clearEvents() {
        while (true) {
            val result = eventChannel.tryReceive()
            if (result.isClosed || result.isFailure) break
        }
    }

    override suspend fun handle(handler: (MidiEvent) -> Unit) {
        handler(eventChannel.receive())
    }
}