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
import mai_onsyn.open_rhythm.bridge.Global
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
        Global.player.blocker.clear()
        while (true) {
            ensureActive()
            try {
                device.handle {
                    when (it) {
                        is NoteEvent -> {
                            if (Global.settings.EnableInputMidiNoteEvent) {
                                if (it.on) {
                                    userActiveKeys[it.pitch] = Global.settings.MidiInteractionColor
                                    if (Global.player.practiceMode) {
                                        Global.player.blocker.press(it.pitch)
                                    }
                                    noteOn(it.pitch, it.velocity)
                                } else {
                                    userActiveKeys.remove(it.pitch)
                                    if (Global.player.practiceMode) {
                                        Global.player.blocker.release(it.pitch)
                                    }
                                    noteOff(it.pitch)
                                }
                                Global.player.sendShortEvent(it.event)
                                Logger.v { "Note input: $it" }
                            }
                        }

                        is MidiCCEvent -> if (Global.settings.EnableInputMidiCCEvent) {
                            Global.player.sendShortEvent(it.event)
                            Logger.v { "CC input: $it" }
                        }

                        is MidiPCEvent -> if (Global.settings.EnableInputMidiPCEvent) {
                            Global.player.sendShortEvent(it.event)
                            Logger.v { "PC input: $it" }
                        }

                        is MidiPBEvent -> if (Global.settings.EnableInputMidiPBEvent) {
                            Global.player.sendShortEvent(it.event)
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
        Global.midiInputDevices.values.forEach { device ->
            device.clearEvents()
            scope.launch { threadBody(device) }
        }

        while (true) {
            ensureActive()
            delay(1.seconds)
            Global.midiInputDevices.values.forEach { device ->
                if (!aliveDevices.contains(device)) {
                    scope.launch {
                        threadBody(device)
                    }
                }
            }
        }
    }
}