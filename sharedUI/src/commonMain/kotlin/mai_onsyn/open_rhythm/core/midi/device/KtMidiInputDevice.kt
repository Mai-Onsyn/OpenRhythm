package mai_onsyn.open_rhythm.core.midi.device

import co.touchlab.kermit.Logger
import dev.atsushieno.ktmidi.MidiAccess
import dev.atsushieno.ktmidi.MidiInput
import dev.atsushieno.ktmidi.MidiPortDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import mai_onsyn.open_rhythm.core.midi.MidiEvent

class KtMidiInputDevice(
    portId: MidiPortDetails,
    access: MidiAccess
) : MidiInputDevice {
    val scope = CoroutineScope(Dispatchers.IO)
    var midiInput: MidiInput? = null

    private val eventChannel = Channel<MidiEvent>(128, BufferOverflow.DROP_OLDEST)
    init {
        scope.launch {
            try {
                midiInput = access.openInput(portId.id)
                midiInput?.setMessageReceivedListener { data, start, length, nanos ->
                    val event =
                        if (data.size == length) MidiEvent.parse(0, data)
                        else MidiEvent.parse(0, data.copyOfRange(start, start + length))
                    eventChannel.trySend(event)
                }
            } catch (e: Exception) {
                Logger.e(e) { "Cannot open input device: ${portId.name}, id: ${portId.id}" }
            }
        }
    }

    override suspend fun handle(handler: (MidiEvent) -> Unit) {
        handler(eventChannel.receive())
    }

    override suspend fun clearEvents() {
        while (true) {
            val result = eventChannel.tryReceive()
            if (result.isClosed || result.isFailure) break
        }
    }

    override suspend fun close() {
        midiInput?.close()
    }
}