package bobko.email.todo.share

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import bobko.email.todo.getAppLabelByPackageName
import bobko.email.todo.getLastUsedAppLabel

class ShareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedString = intent.takeIf { it?.action == Intent.ACTION_SEND }
            ?.getStringExtra(Intent.EXTRA_TEXT)
        val callerLabel = referrer?.host?.let { getAppLabelByPackageName(it) }
            ?: getLastUsedAppLabel()
        saveToEmailAndCloseActivity(sharedString, callerLabel)
    }
}
