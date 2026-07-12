import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mai_onsyn.open_rhythm.core.util.Time
import kotlin.test.Test

class CoroutineTest {

    @Test
    fun testDelay() {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            repeat(10000) {
                val start = Time.nanos
                Time.wait(10)
                val end = Time.nanos
                println(end - start)
            }
        }
        Thread.sleep(10000)  // test only, to prevent the test from finishing before the coroutine has finished
    }


}