package mai_onsyn.open_rhythm.ui.utility

class PriorityQueue<T>(private val comparator: Comparator<T>) {
    private val heap = mutableListOf<T>()

    fun add(element: T) {
        heap.add(element)
        siftUp(heap.size - 1)
    }

    fun poll(): T {
        val result = heap[0]
        val last = heap.removeAt(heap.size - 1)
        if (heap.isNotEmpty()) {
            heap[0] = last
            siftDown(0)
        }
        return result
    }

    fun size(): Int = heap.size
    fun isEmpty(): Boolean = heap.isEmpty()

    private fun siftUp(index: Int) {
        var child = index
        while (child > 0) {
            val parent = (child - 1) / 2
            if (comparator.compare(heap[child], heap[parent]) >= 0) break
            heap.swap(child, parent)
            child = parent
        }
    }

    private fun siftDown(index: Int) {
        var parent = index
        while (true) {
            val left = parent * 2 + 1
            val right = parent * 2 + 2
            var smallest = parent

            if (left < heap.size && comparator.compare(heap[left], heap[smallest]) < 0) smallest = left
            if (right < heap.size && comparator.compare(heap[right], heap[smallest]) < 0) smallest = right
            if (smallest == parent) break

            heap.swap(parent, smallest)
            parent = smallest
        }
    }

    private fun MutableList<T>.swap(i: Int, j: Int) {
        val tmp = this[i]; this[i] = this[j]; this[j] = tmp
    }
}