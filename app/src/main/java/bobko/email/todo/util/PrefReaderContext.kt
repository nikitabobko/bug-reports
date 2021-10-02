package bobko.email.todo.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

open class PrefReaderContext(
    protected val pref: SharedPreferences
) {
    open val <T : Any> PrefKey<T>.value get() = this.getValue(pref)
    operator fun <T> IndexedPrefKey<T>.get(index: Int) = this.getValue(pref, index)
    val <T : Any> PrefKey<T>.liveData: NotNullableLiveData<T> get() = this.getLiveData(pref)
}

class PrefWriterContext(
    pref: SharedPreferences,
    private val editor: SharedPreferences.Editor
) : PrefReaderContext(pref) {
    override var <T : Any> PrefKey<T>.value
        get() = this.getValue(pref)
        set(value) = this.setValue(editor, value)

    operator fun <T> IndexedPrefKey<T>.set(index: Int, value: T?) =
        this.setValue(editor, index, value)
}

fun <T> Context.readPref(body: PrefReaderContext.() -> T) =
    PrefReaderContext(PreferenceManager.getDefaultSharedPreferences(this)).body()

fun <T> Context.writePref(body: PrefWriterContext.() -> T): T {
    val pref = PreferenceManager.getDefaultSharedPreferences(this)
    val editor = pref.edit()
    return PrefWriterContext(pref, editor).body().also { editor.apply() }
}
