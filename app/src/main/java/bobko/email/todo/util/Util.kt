package bobko.email.todo.util

import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LiveData
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

@Composable
fun <T> MutableLiveData<T>.observeAsMutableState(): MutableState<T?> =
    observeAsMutableState(value)

@Composable
fun <R, T : R> MutableLiveData<T>.observeAsMutableState(initial: R): MutableState<R> =
    observeAsState(initial) as MutableState<R>

fun Context.composeView(body: @Composable () -> Unit): View =
    ComposeView(this).apply { setContent(body) }

fun sign(x: Int): Int {
    return when {
        x < 0 -> -1
        x > 0 -> 1
        else -> 0
    }
}

val doesSupportTiles: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
