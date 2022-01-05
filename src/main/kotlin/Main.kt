import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        flowOf(1, 2, 3).collect {
            println(it)
        }
    }
}
