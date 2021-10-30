package bobko.todomail.util

import junit.framework.Assert.fail
import org.junit.Assert
import org.junit.Test

/**
 * Test for [Lens]
 */
class LensTest {
    data class Foo(val bar: Bar, val notDataClass: NotDataClass = NotDataClass(""))
    data class Bar(val text: String)

    class NotDataClass(val text: String) {
        override fun equals(other: Any?) = this === other
        override fun hashCode(): Int = text.hashCode()
    }

    @Test
    fun testSet() {
        val expectedValue = "expected value"
        val init = Foo(Bar(""))
        Assert.assertEquals(expectedValue, Foo::bar.then { ::text }.set(init, expectedValue).bar.text)
        Assert.assertEquals(init.copy(bar = Bar(expectedValue)), Foo::bar.then { ::text }.set(init, expectedValue))
    }

    @Test
    fun testGet() {
        val expectedValue = "expected value"
        Assert.assertEquals(expectedValue, Foo::bar.then { ::text }.get(Foo(Bar(expectedValue))))
    }

    @Test
    fun testDataClassesAssert() {
        try {
            NotDataClass::text.lens
            fail()
        } catch (ignored: IllegalArgumentException) { }
        try {
            Foo::notDataClass.then { ::text }
            fail()
        } catch (ignored: IllegalArgumentException) { }
    }
}
