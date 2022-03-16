import compiler.CompilerRunnable

fun main() {
    val compilerClassloader = CompilerClassloader()
    val pluginClassloader = PluginClassloader(compilerClassloader)
    Class.forName("plugin.PluginMain", true, pluginClassloader).getConstructor().newInstance()
}

//inline fun <T> withClassloader(cl: ClassLoader, body: MyRunner): T {
//    Class.forName("", true, cl)
//    cl.loadClass("MyFunction")
//}

internal class PluginClassloader(val compilerClassloader: CompilerClassloader) : ClassLoader() {
    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String): Class<*> {
        if (name.startsWith("compiler.") || name.endsWith("UnitCompilerRunnable")) {
            return compilerClassloader.loadClass(name)
        }
        if (name.startsWith("plugin.") || name == "kotlin.Pair") {
            val inputStream = getSystemResourceAsStream("${name.replace(".", "/")}.class")!!
            val a = ByteArray(10000000)
            val len = inputStream.read(a)
            inputStream.close()
            return defineClass(name, a, 0, len).also {
                if (it.isAssignableFrom(CompilerRunnable::class.java)) {
                    println("--------- here")
                    return compilerClassloader.loadClass(name)
                }
                if (CompilerRunnable::class.java.isAssignableFrom(it)) {
                    println("--------- here")
                    return compilerClassloader.loadClass(name)
                }
            }
        }
        return super.loadClass(name)
    }
}

internal class CompilerClassloader : ClassLoader() {
    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String): Class<*> {
        if (name.startsWith("plugin.") && !name.endsWith("UnitCompilerRunnable")) {
            error("compiler classloader was asked to load plugin class: $name")
        }
        if (name.startsWith("compiler.") || name == "kotlin.Pair" || name.endsWith("UnitCompilerRunnable")) {
            val inputStream = getSystemResourceAsStream("${name.replace(".", "/")}.class")!!
            val a = ByteArray(10000000)
            val len = inputStream.read(a)
            inputStream.close()
            return defineClass(name, a, 0, len)
        }
        return super.loadClass(name)
    }
}
