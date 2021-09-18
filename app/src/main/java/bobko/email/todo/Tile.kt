package bobko.email.todo

import android.content.Intent
import android.service.quicksettings.TileService

class Tile : TileService() {
    override fun onClick() {
        startActivityAndCollapse(MainActivity.getIntent(this, isStartedFromTile = true).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}
