package mai_onsyn.open_rhythm.ui.utility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.Color
import co.touchlab.kermit.Logger
import kotlinx.coroutines.ensureActive
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.midi.MidiCCEvent
import mai_onsyn.open_rhythm.core.midi.MidiPBEvent
import mai_onsyn.open_rhythm.core.midi.MidiPCEvent
import mai_onsyn.open_rhythm.core.midi.NoteEvent


@Composable
fun BindInputDeviceEvents(userActiveKeys: SnapshotStateMap<Int, Color>) {
    LaunchedEffect(Unit) {
        Singleton.midiInputDevices.values.forEach { it.clearEvents() }
    }
    for (device in Singleton.midiInputDevices.values) {
        LaunchedEffect(Unit) {
            while (true) {
                ensureActive()
                device.handle {
                    when (it) {
                        is NoteEvent -> {
                            if (Singleton.settings.EnableInputMidiNoteEvent) {
                                if (it.on) {
                                    userActiveKeys[it.pitch] = Singleton.settings.KeyboardUserInteractionDisplayColor
                                } else userActiveKeys.remove(it.pitch)
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
            }
        }
    }
}