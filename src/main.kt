import java.util.concurrent.Callable

@OptIn(ExperimentalStdlibApi::class)
fun main() {

//    buildSequence<Int> {
//    }

    buildList<Int> {
    }

    sequence<Int> {
    }

}

fun foo(): Callable<Unit> {
    return Callable { Unit }
}

