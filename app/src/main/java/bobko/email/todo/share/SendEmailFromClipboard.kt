package bobko.email.todo.share

import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.service.quicksettings.TileService
import androidx.activity.ComponentActivity
import androidx.core.content.getSystemService
import bobko.email.todo.getLastUsedAppLabel

class SendEmailFromClipboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(false)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Since Android Q it's necessary for the app to have focus to be able to access clipboard.
        if (hasFocus) {
            val clipboardManager = getSystemService<ClipboardManager>()
            val clipboard = clipboardManager!!.primaryClip?.getItemAt(0)?.text?.toString()
            val callerAppLabelLazy = { getLastUsedAppLabel() }
            saveToEmailAndCloseActivity(clipboard, callerAppLabelLazy)
        }
    }

    companion object {
        fun startActivityAndCollapse(tile: TileService) {
            tile.startActivityAndCollapse(
                Intent(tile, SendEmailFromClipboard::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_NO_ANIMATION or
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
                            Intent.FLAG_ACTIVITY_NO_HISTORY
                }
            )
        }
    }
}
