package bobko.email.todo.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.MutableLiveData

interface NotNullableLiveData<out T : Any> {
    fun getValue(): T

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("valueProperty")
    val value: T
        get() = getValue()
}

open class NotNullableMutableLiveData<T : Any>(value: T) : MutableLiveData<T>(value),
    NotNullableLiveData<T> {
    override fun getValue(): T =
        super.getValue() ?: error("It's not possible. It must be initialized")

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("valueProperty")
    @set:JvmName("setValueProperty")
    override var value: T
        get() = super.value
        set(value) {
            setValue(value)
        }
}

@Composable
fun <T : Any> NotNullableLiveData<T>.observeAsNotNullableState(): State<T> =
    (this as NotNullableMutableLiveData<T>).observeAsState(value)

@Composable
fun <T : Any> NotNullableMutableLiveData<T>.observeAsNotNullableMutableState(): MutableState<T> =
    this.observeAsState(value) as MutableState<T>
