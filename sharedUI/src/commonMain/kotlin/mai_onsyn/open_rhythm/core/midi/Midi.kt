package mai_onsyn.open_rhythm.core.midi

class Midi(
    val name: String,
    val ppq: Int,
    val totalTicks: Int,
    val tracks: MutableList<MidiTrack> = mutableListOf(),
    val tempoEvents: MutableList<TempoEvent> = mutableListOf(),
    val timeSignatureEvents: MutableList<TimeSignatureEvent> = mutableListOf(),
    val startTick: Int = 0,
    val endTick: Int = totalTicks,
    val ccChangeTimeline: Array<CCTimeline> = Array(16) { CCTimeline() },
    val pcChangeTimeline: Array<SingleChangeTimeline> = Array(16) { SingleChangeTimeline() },
    val pbChangeTimeline: Array<SingleChangeTimeline> = Array(16) { SingleChangeTimeline() },
    val path: String = ""
) {
    val tempoMap = TempoMap(ppq, tempoEvents)
    val totalNotes: Int
        get() {
            var sum = 0
            tracks.forEach { sum += it.notes.size }
            return sum
        }
}

fun Midi.take(trackNumber: Int): Midi {
    return if (trackNumber !in this.tracks.indices) Midi(
        name, ppq, 4 * ppq, mutableListOf(), tempoEvents, timeSignatureEvents,
        ccChangeTimeline = ccChangeTimeline,
        pcChangeTimeline = pcChangeTimeline,
        pbChangeTimeline = pbChangeTimeline,
        path = path
    ) else Midi(
        name, ppq, totalTicks, mutableListOf(tracks[trackNumber]), tempoEvents, timeSignatureEvents,
        ccChangeTimeline = ccChangeTimeline,
        pcChangeTimeline = pcChangeTimeline,
        pbChangeTimeline = pbChangeTimeline,
        path = path
    )
}