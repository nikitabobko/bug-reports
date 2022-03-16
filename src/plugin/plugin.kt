package plugin

import compiler.CompilerClass
import compiler.CompilerRunnable
import compiler.withCompilerClassloader

class PluginMain {
    init {
        val a1 = CompilerClass(1)
        val a2 = CompilerClass(2)
        println(a1::class.java.classLoader == a2::class.java.classLoader)
        println(CompilerClass::class.java.classLoader == a2::class.java.classLoader)
        a2.foo(a1)

        withCompilerClassloader(UnitCompilerRunnable(a2))
        println("PluginMain is initialized by + " + this::class.java.classLoader)
    }
}

class UnitCompilerRunnable(private val a2: CompilerClass) : CompilerRunnable<Unit> {
    override fun run() {
        println("inside withCompilerClassloader lambda: anon classloader is " + object {}::class.java.classLoader)
        println("inside withCompilerClassloader lambda: kotlin.Pair classloader is " + Pair::class.java.classLoader)
        return a2.pair("foo" to "bar")
    }
}
