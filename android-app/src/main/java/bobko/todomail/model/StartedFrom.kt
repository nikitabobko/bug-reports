package bobko.todomail.model

import bobko.todomail.model.pref.PrefManager
import bobko.todomail.util.PrefKey

enum class StartedFrom(
    val text: String,
    val prefillPrefKey: PrefKey<Boolean>?,
    val closeAfterSendPrefKey: PrefKey<Boolean>
) {
    Launcher(
        "started from Launcher",
        PrefManager.prefillWithClipboardWhenStartedFromLauncher,
        PrefManager.closeDialogAfterSendWhenStartedFromLauncher
    ),
    Tile(
        "started from Tile",
        PrefManager.prefillWithClipboardWhenStartedFromTile,
        PrefManager.closeDialogAfterSendWhenStartedFromTile
    ),
    Sharesheet(
        "started from Sharesheet",
        null,
        PrefManager.closeDialogAfterSendWhenStartedFromSharesheet
    )
}
