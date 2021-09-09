package bobko.email.todo

import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SendEmailFromClipboardAutocloseableActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(false)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Since Android Q it's necessary for the app to have focus to be able to access clipboard.
        if (hasFocus) {
            val clipboardContent =
                getSystemService<ClipboardManager>()?.primaryClip?.getItemAt(0)?.text?.toString()
            lifecycleScope.launch(Dispatchers.Main) {
                if (clipboardContent != null) {
                    withContext(Dispatchers.IO) {
                        EmailManager.sendEmailToMyself(clipboardContent, "")
                    }
                }
                Toast.makeText(
                    this@SendEmailFromClipboardAutocloseableActivity,
                    when (clipboardContent) {
                        null -> "Failed or clipboard is empty"
                        else -> "Email '${clipboardContent.take(10)}' is sent"
                    },
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    companion object {
        fun startActivityAndCollapse(tile: TileService) {
            tile.startActivityAndCollapse(
                Intent(tile, SendEmailFromClipboardAutocloseableActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_NO_ANIMATION or
                            Intent.FLAG_ACTIVITY_NO_HISTORY
                }
            )
        }
    }
}
