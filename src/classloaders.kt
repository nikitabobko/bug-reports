import compiler.CompilerRunnable
import java.net.URLClassLoader

fun main() {
    val compilerClassloader = CompilerClassloader()
    val pluginClassloader = PluginClassloader(compilerClassloader)
    compilerClassloader.pluginClassloader = pluginClassloader
    Class.forName("plugin.PluginMain", true, pluginClassloader).getConstructor().newInstance()
}

//inline fun <T> withClassloader(cl: ClassLoader, body: MyRunner): T {
//    Class.forName("", true, cl)
//    cl.loadClass("MyFunction")
//}

internal class PluginClassloader(val compilerClassloader: CompilerClassloader) : ClassLoader() {
    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String): Class<*> {
        if (name.startsWith("compiler.") || name.contains("CompilerRunnable")) {
            return compilerClassloader.loadClass(name)
        }
        if (name.startsWith("plugin.") || name == "kotlin.Pair") {
            if (name != "kotlin.Pair") {
                findLoadedClass(name)?.takeIf { it.classLoader == this }?.let { return it }
            }
            val inputStream = getSystemResourceAsStream("${name.replace(".", "/")}.class")!!
            val a = ByteArray(10000000)
            val len = inputStream.read(a)
            inputStream.close()
            return defineClass(name, a, 0, len)
        }
        return super.loadClass(name)
    }
}

internal class CompilerClassloader : ClassLoader() {
    lateinit var pluginClassloader: PluginClassloader

    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String): Class<*> {
        if (name.startsWith("plugin.") && !name.contains("CompilerRunnable")) {
            error("compiler classloader was asked to load plugin class: $name")
        }
        if (name.startsWith("compiler.") || name == "kotlin.Pair" || name.contains("CompilerRunnable")) {
            if (name != "kotlin.Pair") {
                findLoadedClass(name)?.takeIf { it.classLoader == this }?.let { return it }
            }
//            findLoadedClass(name)?.takeIf { it.classLoader == this }?.let { return it }
            val inputStream = getSystemResourceAsStream("${name.replace(".", "/")}.class")!!
            val a = ByteArray(10000000)
            val len = inputStream.read(a)
            inputStream.close()
            return defineClass(name, a, 0, len)
        }
        return super.loadClass(name)
    }
}
