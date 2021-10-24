package bobko.todomail.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

typealias InitializedLiveData<T> = AbstractInitializedLiveData<T, LiveData<T>>

open class AbstractInitializedLiveData<T : Any, out LD : LiveData<T>>(val liveData: LD) {
    init {
        require(liveData.value != null) { "LiveData=$liveData must be initialized" }
    }
    open val value: T get() = liveData.value!!
}

class MutableInitializedLiveData<T : Any>(liveData: MutableLiveData<T>) :
    AbstractInitializedLiveData<T, MutableLiveData<T>>(liveData) {
    override var value: T
        get() = super.value
        set(value) {
            liveData.value = value
        }
}

fun <T : Any> liveDataOf(value: T) = InitializedLiveData(MutableLiveData(value))

fun <T : Any> mutableLiveDataOf(value: T) =
    MutableInitializedLiveData(MutableLiveData(value))

@Composable
fun <T : Any> AbstractInitializedLiveData<T, LiveData<T>>.observeAsState(): State<T> =
    liveData.observeAsState(value)

fun <T : Any, O : Any, R : Any> AbstractInitializedLiveData<T, LiveData<T>>.then(
    other: AbstractInitializedLiveData<O, LiveData<O>>,
    merge: (T, O) -> R
): AbstractInitializedLiveData<R, LiveData<R>> {
    val firstSource = this
    return AbstractInitializedLiveData(
        MediatorLiveData<R>().apply {
            addSource(firstSource.liveData) {
                value = merge(it, other.value)
            }
            addSource(other.liveData) {
                value = merge(firstSource.value, it)
            }
            value = merge(firstSource.value, other.value)
        }
    )
}
