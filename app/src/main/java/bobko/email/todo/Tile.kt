package bobko.email.todo

import android.service.quicksettings.TileService
import bobko.email.todo.share.SendEmailFromClipboard

class Tile : TileService() {
    override fun onClick() {
        SendEmailFromClipboard.startActivityAndCollapse(this)
    }
}
