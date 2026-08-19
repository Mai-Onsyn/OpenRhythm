package mai_onsyn.open_rhythm.core.midi

private const val NANOS_PER_MINUTE = 60_000_000_000.0

class TempoMap(
    private val ppq: Int,
    tempoEvents: List<TempoEvent>,
) {
    private data class Entry(
        val tick: Long,
        val bpm: Double,
        val nanoAtTick: Double,
        val nanosPerTick: Double,
    )

    private val entries: List<Entry>

    init {
        require(ppq > 0)
        require(tempoEvents.isNotEmpty())

        val events = tempoEvents.sortedBy { it.tick }

        entries = buildList(events.size) {
            var nanoAtTick = events[0].tick *
                    (NANOS_PER_MINUTE / (ppq * events[0].bpm))

            add(
                Entry(
                    tick = events[0].tick,
                    bpm = events[0].bpm,
                    nanoAtTick = nanoAtTick,
                    nanosPerTick = NANOS_PER_MINUTE / (ppq * events[0].bpm),
                )
            )

            for (i in 1 until events.size) {
                val previous = this.last()
                val event = events[i]

                nanoAtTick +=
                    (event.tick - previous.tick) * previous.nanosPerTick

                val nanosPerTick =
                    NANOS_PER_MINUTE / (ppq * event.bpm)

                add(
                    Entry(
                        tick = event.tick,
                        bpm = event.bpm,
                        nanoAtTick = nanoAtTick,
                        nanosPerTick = nanosPerTick,
                    )
                )
            }
        }
    }

    fun tickToNanos(tick: Double): Double {
        val index = findTempoBy(tick) { it.tick }
        val entry = entries[index]

        return entry.nanoAtTick +
                (tick - entry.tick) * entry.nanosPerTick
    }

    fun nanosToTick(nanos: Double): Double {
        val index = findTempoBy(nanos) { it.nanoAtTick }
        val entry = entries[index]

        return entry.tick +
                (nanos - entry.nanoAtTick) / entry.nanosPerTick
    }

    private fun <T: Number> findTempoBy(tick: Double, method: (Entry) -> T): Int {
        var low = 0
        var high = entries.lastIndex

        while (low <= high) {
            val mid = (low + high) ushr 1

            if (method(entries[mid]).toDouble() <= tick) {
                low = mid + 1
            } else {
                high = mid - 1
            }
        }

        return high.coerceAtLeast(0)
    }

//    private fun findTempoByNanos(nanos: Double): Int {
//        var low = 0
//        var high = entries.lastIndex
//
//        while (low <= high) {
//            val mid = (low + high) ushr 1
//
//            if (entries[mid].nanoAtTick <= nanos) {
//                low = mid + 1
//            } else {
//                high = mid - 1
//            }
//        }
//
//        return high.coerceAtLeast(0)
//    }
}