package bobko.email.todo

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import androidx.core.content.getSystemService

fun Context.getAppLabelByPackageName(packageName: String): String? {
    return try {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0))
            .toString()
    } catch (ex: NameNotFoundException) {
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
