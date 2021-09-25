package bobko.email.todo.util

import android.content.SharedPreferences

open class PrefReaderContext(protected val pref: SharedPreferences) {
    open val <T> PrefKey<T>.value get() = this.getValue(pref)
    operator fun <T> IndexedPrefKey<T>.get(index: Int) = this.getValue(pref, index)
}

class PrefWriterContext(
    pref: SharedPreferences,
    private val editor: SharedPreferences.Editor
) : PrefReaderContext(pref) {
    override var <T> PrefKey<T>.value
        get() = this.getValue(pref)
        set(value) = this.setValue(editor, value)

    operator fun <T> IndexedPrefKey<T>.set(index: Int, value: T?) =
        this.setValue(editor, index, value)
}

fun <T> SharedPreferences.read(body: PrefReaderContext.() -> T) =
    PrefReaderContext(this).body()

fun <T> SharedPreferences.write(body: PrefWriterContext.() -> T): T {
    val editor = this.edit()
    return PrefWriterContext(this, editor).body().also { editor.apply() }
}
