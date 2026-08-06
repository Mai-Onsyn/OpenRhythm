package mai_onsyn.open_rhythm.ui.utility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.Color
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.midi.MidiCCEvent
import mai_onsyn.open_rhythm.core.midi.MidiPBEvent
import mai_onsyn.open_rhythm.core.midi.MidiPCEvent
import mai_onsyn.open_rhythm.core.midi.NoteEvent
import mai_onsyn.open_rhythm.core.midi.device.MidiInputDevice
import kotlin.time.Duration.Companion.seconds


@Composable
fun BindInputDeviceEvents(
    userActiveKeys: SnapshotStateMap<Int, Color>,
    noteOn: (Int, Int) -> Unit = { _, _ ->  },
    noteOff: (Int) -> Unit = {}
) {
    val aliveDevices = remember { mutableListOf<MidiInputDevice>() }
    val scope = rememberCoroutineScope()

    suspend fun CoroutineScope.threadBody(device: MidiInputDevice) {
        aliveDevices.add(device)
        while (true) {
            ensureActive()
            try {
                device.handle {
                    when (it) {
                        is NoteEvent -> {
                            if (Singleton.settings.EnableInputMidiNoteEvent) {
                                if (it.on) {
                                    userActiveKeys[it.pitch] = Singleton.settings.KeyboardUserInteractionDisplayColor
                                    noteOn(it.pitch, it.velocity)
                                } else {
                                    userActiveKeys.remove(it.pitch)
                                    noteOff(it.pitch)
                                }
                                Singleton.player.sendShortEvent(it.event)
                                Logger.v { "Note input: $it" }
                            }
                        }

                        is MidiCCEvent -> if (Singleton.settings.EnableInputMidiCCEvent) {
                            Singleton.player.sendShortEvent(it.event)
                            Logger.v { "CC input: $it" }
                        }

                        is MidiPCEvent -> if (Singleton.settings.EnableInputMidiPCEvent) {
                            Singleton.player.sendShortEvent(it.event)
                            Logger.v { "PC input: $it" }
                        }

                        is MidiPBEvent -> if (Singleton.settings.EnableInputMidiPBEvent) {
                            Singleton.player.sendShortEvent(it.event)
                            Logger.v { "PB input: $it" }
                        }

                        else -> Logger.v { "Unknown input: $it" }
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                break
            }
        }
        aliveDevices.remove(device)
    }
    LaunchedEffect(Unit) {
        Singleton.midiInputDevices.values.forEach { it.clearEvents() }

        while (true) {
            ensureActive()
            Singleton.midiInputDevices.values.forEach { device ->
                if (!aliveDevices.contains(device)) {
                    scope.launch {
                        threadBody(device)
                    }
                }
            }
            delay(1.seconds)
        }
    }

    for (device in Singleton.midiInputDevices.values) {
        LaunchedEffect(Unit) {
            threadBody(device)
        }
    }
}