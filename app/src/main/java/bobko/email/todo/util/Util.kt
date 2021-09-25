package bobko.email.todo.util

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.getSystemService
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

fun Context.getLastUsedAppLabel(): String? {
    val time = System.currentTimeMillis()
    val stats = getSystemService<UsageStatsManager>()!!
        .queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            time - 1000 * 10, // We get usage stats for the last 10 seconds
            time
        )
    val packageName = stats.asSequence()
        .filter { it.packageName != packageName && it.packageName != "android" }
        .maxByOrNull { it.lastTimeUsed }
        .orElse { return null }
        .packageName
    return getAppLabelByPackageName(packageName)
}

inline fun <T> T?.orElse(block: () -> T): T = this ?: block()

fun Context.showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

@Composable
fun <T> MutableLiveData<T>.observeAsMutableState(): MutableState<T> {
    val state by this.observeAsState()
    return object : MutableState<T> {
        override var value: T
            get() = state as T
            set(value) {
                this@observeAsMutableState.value = value
            }

        override fun component1(): T = value
        override fun component2(): (T) -> Unit = { value = it }
    }
}

fun Context.composeView(body: @Composable () -> Unit): View =
    ComposeView(this).apply { setContent(body) }
