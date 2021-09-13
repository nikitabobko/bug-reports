package bobko.email.todo.share

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import bobko.email.todo.EmailManager
import bobko.email.todo.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun ComponentActivity.saveToEmailAndCloseActivity(
    text: String?,
    callerAppLabelLazy: () -> String?
) {
    lifecycleScope.launch {
        if (text != null) {
            val callerAppLabel = callerAppLabelLazy() ?: "*"
            val isWork = callerAppLabel == "Slack"
            withContext(Dispatchers.IO) {
                EmailManager.sendEmailToMyself("From '$callerAppLabel'", text, isWork)
            }
        }
        showToast(if (text != null) "Saved to email" else "Failed")
        finish()
    }
}
