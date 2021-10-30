package bobko.todomail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

fun <T> onUiThread(block: suspend () -> T) = runBlocking {
    withContext(Dispatchers.Main) {
        block()
    }
}
