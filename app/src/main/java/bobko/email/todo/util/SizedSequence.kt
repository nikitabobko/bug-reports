package bobko.email.todo.util

class SizedSequence<T>(private val sequence: Sequence<T>, private val size: Int) : Sequence<T> {
    override fun iterator() = sequence.iterator()

    fun count() = size
}
