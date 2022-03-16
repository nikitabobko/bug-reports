package compiler

class CompilerClass(val int: Int) {
    init {
        println("CompilerClass is initiliazed by " + this::class.java.classLoader)
    }

    fun foo(a: CompilerClass) {
        println(a.int)
    }

    fun pair(pair: Pair<String, String>) {
        println(pair.first)
        println(pair.second)
    }
}

interface CompilerRunnable<T> {
    fun run(): T
}

fun <T> withCompilerClassloader(body: CompilerRunnable<T>): T {
    println("inside withCompilerClassloader: anon classloader is " + object {}::class.java.classLoader)
    return body.run()
}
