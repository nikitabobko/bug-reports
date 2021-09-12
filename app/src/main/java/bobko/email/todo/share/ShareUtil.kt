package bobko.email.todo.share

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import bobko.email.todo.EmailManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun ComponentActivity.saveToEmailAndCloseActivity(text: String?, callerAppLabel: String?) {
    lifecycleScope.launch {
        @Suppress("NAME_SHADOWING")
        val callerAppLabel = callerAppLabel ?: "*"
        val isWork = callerAppLabel == "Slack"
        if (text != null) {
            withContext(Dispatchers.IO) {
                EmailManager.sendEmailToMyself("From $callerAppLabel", text, isWork)
            }
        }
        Toast.makeText(
            this@saveToEmailAndCloseActivity,
            if (text != null) "Saved to email" else "Failed",
            Toast.LENGTH_LONG
        ).show()
        finish()
    }
}
