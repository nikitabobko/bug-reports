package bobko.todomail.util

import android.content.SharedPreferences
import androidx.annotation.MainThread
import java.lang.ref.WeakReference
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface SharedPrefSerializer<T> {
    fun serialize(editor: SharedPreferences.Editor, value: T)
    fun deserialize(pref: SharedPreferences): T
}

class StringSharedPrefSerializer : SharedPrefSerializer<String> { // REPORT: Try calling Alt + Enter

}

fun <T> sharedPref(
    serializer: (SharedPreferences.Editor, value: T) -> Unit,
    deserializer: (SharedPreferences) -> T
) = object : SharedPrefSerializer<T> {
    override fun serialize(editor: SharedPreferences.Editor, value: T) = serializer(editor, value)
    override fun deserialize(pref: SharedPreferences) = deserializer(pref)
}

fun stringSharedPref(defaultValue: String) {

}

sealed class AbstractPrefKey<T, Self : AbstractPrefKey<T, Self>>(
    private val clazz: Class<T>,
    private val key: String,
    private val defaultValue: T,
    private val ignoreIndex: Boolean
) : ReadOnlyProperty<Any?, Self> {

    @MainThread
    protected fun getValueInternal(pref: SharedPreferences, index: Int): T {
        require(!ignoreIndex || index == 0)
        val realKey = if (ignoreIndex) key else key + index
        if (!pref.contains(realKey)) {
            return defaultValue
        }
        return when (clazz) {
            Int::class.java, java.lang.Integer::class.java -> pref.getInt(realKey, 0)
            String::class.java, java.lang.String::class.java -> pref.getString(realKey, null)
            Boolean::class.java, java.lang.Boolean::class.java -> pref.getBoolean(realKey, false)
            else -> error("$clazz isn't supported")
        } as T
    }

    @MainThread
    protected fun setValueInternal(editor: SharedPreferences.Editor, index: Int, value: T) {
        require(!ignoreIndex || index == 0)
        val realKey = if (ignoreIndex) key else key + index
        if (value == null) {
            editor.remove(realKey)
            return
        }
        when (clazz) {
            Int::class.java, java.lang.Integer::class.java -> editor.putInt(realKey, value as Int)
            String::class.java, java.lang.String::class.java -> editor.putString(realKey, value as String)
            Boolean::class.java, java.lang.Boolean::class.java -> editor.putBoolean(realKey, value as Boolean)
            else -> error("$clazz isn't supported")
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>) = this as Self
}

class PrefKey<T : Any>(clazz: Class<T>, key: String, defaultValue: T) :
    AbstractPrefKey<T, PrefKey<T>>(clazz, key, defaultValue, ignoreIndex = true) {

    private var _liveData = WeakReference<MutableInitializedLiveData<T>>(null)

    /**
     * this [PrefKey] must be a singleton for this feature to work
     */
    fun getLiveData(pref: SharedPreferences): MutableInitializedLiveData<T> =
        _liveData.get() ?: mutableLiveDataOf(getValue(pref)).also {
            _liveData = WeakReference(it)
        }

    fun getValue(pref: SharedPreferences) = getValueInternal(pref, 0)
    fun setValue(editor: SharedPreferences.Editor, value: T) {
        setValueInternal(editor, 0, value)
        _liveData.get()?.value = value
    }

    companion object {
        inline fun <reified T : Any> delegate(defaultValue: T) =
            PropertyDelegateProvider<Any?, PrefKey<T>> { _, property ->
                PrefKey(T::class.java, property.name, defaultValue)
            }
    }
}

class IndexedPrefKey<T>(clazz: Class<T>, key: String) :
    AbstractPrefKey<T?, IndexedPrefKey<T>>(clazz as Class<T?>, key, null, ignoreIndex = false) {

    fun getValue(pref: SharedPreferences, index: Int) = getValueInternal(pref, index)
    fun setValue(editor: SharedPreferences.Editor, index: Int, value: T?) =
        setValueInternal(editor, index, value)

    companion object {
        inline fun <reified T> delegate() =
            PropertyDelegateProvider<Any?, IndexedPrefKey<T>> { _, property ->
                IndexedPrefKey(T::class.java, property.name)
            }
    }
}
