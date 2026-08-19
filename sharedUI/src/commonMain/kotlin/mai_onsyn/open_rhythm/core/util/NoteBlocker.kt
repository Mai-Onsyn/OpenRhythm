package mai_onsyn.open_rhythm.core.util

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CompletableDeferred

class NoteBlocker {

    private val activeNotes = mutableSetOf<Int>()

    private var requiredNotes: Set<Int>? = null
    private var waiter: CompletableDeferred<Unit>? = null

    fun press(note: Int) {
        activeNotes += note

        val required = requiredNotes ?: return

        if (activeNotes.containsAll(required)) {
            waiter?.complete(Unit)
        }
    }

    fun release(note: Int) {
        activeNotes -= note
    }

    fun clear() = activeNotes.clear()

    suspend fun await(notes: Set<Int>) {
        if (activeNotes.containsAll(notes)) {
            return
        }

        requiredNotes = notes
        waiter = CompletableDeferred()

        if (activeNotes.containsAll(notes)) {
            waiter!!.complete(Unit)
        }

        try {
            waiter!!.await()
        } finally {
            requiredNotes = null
            waiter = null
        }
    }
}