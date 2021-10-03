package bobko.email.todo.util

import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.MutableLiveData

fun Context.getAppLabelByPackageName(packageName: String): String? {
    return try {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0))
            .toString()
    } catch (ex: NameNotFoundException) {
        // TODO logging
        null
    }
}

inline fun <T> T?.orElse(block: () -> T): T = this ?: block()

fun Context.showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.composeView(body: @Composable () -> Unit): View =
    ComposeView(this).apply { setContent(body) }

@Composable
fun <T : Any> MutableInitializedLiveData<T>.observeAsMutableState(): MutableState<T> {
    val mutableSource = this
    val state = observeAsState()

    return object : MutableState<T> {
        override var value: T
            get() = state.value
            set(value) {
                mutableSource.value = value
            }

        override fun component1(): T = value
        override fun component2(): (T) -> Unit = { value = it }
    }
}

fun sign(x: Int): Int {
    return when {
        x < 0 -> -1
        x > 0 -> 1
        else -> 0
    }
}

val doesSupportTiles: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
