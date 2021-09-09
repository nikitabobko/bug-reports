package bobko.email.todo

import android.service.quicksettings.TileService

class Tile : TileService() {
    override fun onClick() {
        SendEmailFromClipboardAutocloseableActivity.startActivityAndCollapse(this)
    }
}
