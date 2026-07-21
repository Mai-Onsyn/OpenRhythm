package mai_onsyn.open_rhythm.core.midi.device

import mai_onsyn.open_rhythm.core.midi.MidiEvent

interface MidiInputDevice {
    suspend fun handle(handler: (MidiEvent) -> Unit)

    suspend fun clearEvents()
}