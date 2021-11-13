package bobko.todomail.util

import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

/**
 * Test - [bobko.todomail.util.LensTest]
 */
class Lens<A : Any, B>(val getter: (obj: A) -> B, val setter: (obj: A, newValue: B) -> A) {
    fun get(source: A): B = getter(source)
    fun set(source: A, newValue: B): A = setter(source, newValue)
}

inline val <reified A : Any, B> KProperty1<A, B>.lens: Lens<A, B>
    get() {
        require(A::class.isData)
        val property = this
        return Lens(property, { obj, newValue -> obj.copy(property, newValue) })
    }

fun <A : Any, B : Any, C : Any> Lens<A, B>.then(other: Lens<B, C>): Lens<A, C> = Lens(
    { obj -> other.getter(getter(obj)) },
    { obj, newValue -> setter(obj, other.setter(getter(obj), newValue)) }
)

inline fun <A : Any, reified B : Any, C : Any> Lens<A, B>.then(crossinline other: B.() -> KProperty0<C>): Lens<A, C> {
    require(B::class.isData)
    return then(
        Lens(
            { obj -> other(obj).get() },
            { obj, newValue -> obj.copy(obj.other(), newValue) }
        )
    )
}

inline fun <reified A : Any, reified B : Any, C : Any> KProperty1<A, B>.then(crossinline other: B.() -> KProperty0<C>) =
    this.lens.then(other)
