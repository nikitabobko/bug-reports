package bobko.todomail.util

import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

/**
 * Test - [bobko.todomail.util.LensTest]
 */
class Lens<A : Any, B>(val getter: A.() -> B, val setter: A.(B) -> A) {
    fun get(source: A): B = source.getter()
    fun set(source: A, newValue: B): A = source.setter(newValue)
}

inline val <reified A : Any, B> KProperty1<A, B>.lens: Lens<A, B>
    get() {
        require(A::class.isData)
        val property = this
        return Lens(property, { copy(property, it) })
    }

fun <A : Any, B : Any, C : Any> Lens<A, B>.then(other: Lens<B, C>): Lens<A, C> = Lens(
    {
        val secondGetter = other.getter
        getter().secondGetter()
    },
    {
        val secondSetter = other.setter
        setter(getter().secondSetter(it))
    }
)

inline fun <A : Any, reified B : Any, C : Any> Lens<A, B>.then(crossinline other: B.() -> KProperty0<C>): Lens<A, C> {
    require(B::class.isData)
    return then(Lens({ other().get() }, { copy(other(), it) }))
}

inline fun <reified A : Any, reified B : Any, C : Any> KProperty1<A, B>.then(crossinline other: B.() -> KProperty0<C>) =
    lens.then(other)
