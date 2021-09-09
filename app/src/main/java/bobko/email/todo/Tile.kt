package bobko.email.todo

import android.content.ClipboardManager
import android.service.quicksettings.TileService
import android.content.Intent
import android.widget.Toast
import androidx.core.content.getSystemService
import kotlinx.coroutines.*


class Tile : TileService() {
    override fun onClick() {
        // Close notifications
        val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        applicationContext.sendBroadcast(closeIntent)

        startActivity(Intent(applicationContext, GetClipboardTextActivity::class.java).apply {
            // Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
//        CoroutineScope(Dispatchers.Main).launch {
//            val text = applicationContext.getSystemService<ClipboardManager>()
//                ?.primaryClip?.getItemAt(0)?.text?.toString()
//            if (text != null) {
//                withContext(Dispatchers.IO) {
//                    GmailManager.sendEmailToMyself(text, "")
//                }
//            }
//            Toast.makeText(
//                applicationContext,
//                if (text != null) "Email '${text.take(10)}' is sent" else "Clipboard is empty",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
    }
}
