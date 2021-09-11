package bobko.email.todo

import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.core.content.getSystemService

fun String.ellipsis(n: Int): String {
    return take(n).trim() + if (length > n) "..." else ""
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
        .filter { it.packageName != packageName }
        .maxByOrNull { it.lastTimeUsed }
        .orElse { return null }
        .packageName
    return packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0))
        .toString()
}

inline fun <T> T?.orElse(block: () -> T): T = this ?: block()
