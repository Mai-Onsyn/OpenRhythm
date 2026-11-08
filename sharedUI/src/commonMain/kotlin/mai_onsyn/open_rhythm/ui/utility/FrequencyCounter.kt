package mai_onsyn.open_rhythm.ui.utility

import mai_onsyn.open_rhythm.core.util.Time
import kotlin.math.max

class FrequencyCounter(private val capacity: Int = 1024) {

    private var head = 0
    private var count = 0

    private val timestamps = LongArray(capacity)
    private val deltaTimes = UIntArray(capacity)

    private var lastTickTime: Long = 0L

    private fun getIndexOffset(backwardOffset: Int): Int {
        return (head + capacity - 1 - backwardOffset) % capacity
    }

    fun tick() {
        val currentTime = Time.micros

        val delta: UInt =
            if (lastTickTime == 0L) 0u
            else (currentTime - lastTickTime).toUInt()

        timestamps[head] = currentTime
        deltaTimes[head] = delta

        head = (head + 1) % capacity
        if (count < capacity) {
            count++
        }

        lastTickTime = currentTime
    }

    fun getLastTickTime(): Long = lastTickTime

    fun getAverageFrequency(lastNSeconds: Float = 1f): Float {
        if (count < 2) return 0.0f

        val now = Time.micros
        val cutoffTime = now - (lastNSeconds * 1_000_000.0).toLong()

        var validSamples = 0
        for (i in 0 until count) {
            val idx = getIndexOffset(i)
            if (timestamps[idx] < cutoffTime) {
                break
            }
            validSamples++
        }

        if (validSamples < 2) return 0.0f

        val newestTime = timestamps[getIndexOffset(0)]
        val oldestTime = timestamps[getIndexOffset(validSamples - 1)]

        val totalSeconds = (newestTime - oldestTime).toFloat() / 1_000_000.0f
        if (totalSeconds <= 0.0f) return 0.0f

        return (validSamples - 1).toFloat() / totalSeconds
    }

    fun getOnePercentLowFrequency(lastNSeconds: Float = 1f): Float {
        if (count < 2) return 0.0f

        val now = Time.micros
        val cutoffTime = now - (lastNSeconds * 1_000_000.0).toLong()

        val localDeltas = ArrayList<UInt>(count)

        for (i in 0 until count) {
            val idx = getIndexOffset(i)
            if (timestamps[idx] < cutoffTime) break

            val delta = deltaTimes[idx]
            if (delta > 0u) {
                localDeltas.add(delta)
            }
        }

        if (localDeltas.isEmpty()) return 0.0f

        val lowCount = max(1, (localDeltas.size * 0.01f).toInt())
        val targetIndex = localDeltas.size - lowCount

        // 等价于 std::ranges::nth_element 之后再取尾部 lowCount 个元素之和
        localDeltas.sort()

        var totalDeltaMicros = 0uL
        for (i in targetIndex until localDeltas.size) {
            totalDeltaMicros += localDeltas[i].toULong()
        }

        val avgDeltaSeconds =
            (totalDeltaMicros.toFloat() / lowCount.toFloat()) / 1_000_000.0f

        return if (avgDeltaSeconds > 0.0f) 1.0f / avgDeltaSeconds else 0.0f
    }

    fun clear() {
        head = 0
        count = 0
        lastTickTime = 0L
    }
}