package bobko.email.todo.util

import android.content.SharedPreferences
import kotlin.properties.ReadOnlyProperty

sealed class AbstractPrefKey<T>(
    private val clazz: Class<T>,
    private val key: String,
    private val defaultValue: T,
    private val ignoreIndex: Boolean
) {
    protected fun getValueInternal(pref: SharedPreferences, index: Int): T {
        require(!ignoreIndex || index == 0)
        val realKey = if (ignoreIndex) key else key + index
        return when (clazz) {
            java.lang.Integer::class.java, Int::class.java -> pref.getInt(realKey, Int.MIN_VALUE)
                .takeIf { it != Int.MIN_VALUE } ?: defaultValue
            String::class.java -> pref.getString(realKey, null) ?: defaultValue
            else -> error("$clazz isn't supported")
        } as T
    }

    protected fun setValueInternal(editor: SharedPreferences.Editor, index: Int, value: T) {
        require(!ignoreIndex || index == 0)
        val realKey = if (ignoreIndex) key else key + index
        if (value == null) {
            editor.remove(realKey)
            return
        }
        when (clazz) {
            Int::class.java, java.lang.Integer::class.java -> editor.putInt(realKey, value as Int)
            String::class.java -> editor.putString(realKey, value as String)
            else -> error("$clazz isn't supported")
        }
    }
}

class PrefKey<T>(clazz: Class<T>, key: String, defaultValue: T) :
    AbstractPrefKey<T>(clazz, key, defaultValue, ignoreIndex = true) {

    fun getValue(pref: SharedPreferences) = getValueInternal(pref, 0)
    fun setValue(editor: SharedPreferences.Editor, value: T) =
        setValueInternal(editor, 0, value)

    companion object {
        inline fun <reified T> delegate(defaultValue: T) =
            ReadOnlyProperty<Any?, PrefKey<T>> { _, property ->
                PrefKey(T::class.java, property.name, defaultValue)
            }
    }
}

class IndexedPrefKey<T>(clazz: Class<T>, key: String) :
    AbstractPrefKey<T?>(clazz as Class<T?>, key, null, ignoreIndex = false) {

    fun getValue(pref: SharedPreferences, index: Int) = getValueInternal(pref, index)
    fun setValue(editor: SharedPreferences.Editor, index: Int, value: T?) =
        setValueInternal(editor, index, value)

    companion object {
        inline fun <reified T> delegate() =
            ReadOnlyProperty<Any?, IndexedPrefKey<T>> { _, property ->
                IndexedPrefKey(T::class.java, property.name)
            }
    }
}
