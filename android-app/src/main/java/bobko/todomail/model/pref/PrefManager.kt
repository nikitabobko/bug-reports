package bobko.todomail.model.pref

import android.content.Context
import bobko.todomail.model.SendReceiveRoute
import bobko.todomail.util.*
import kotlinx.coroutines.flow.*
import java.lang.ref.WeakReference

object PrefManager {
    private val numberOfAccounts by PrefKey.delegate(defaultValue = 0)
    private var accounts = WeakReference<MutableInitializedLiveData<List<SendReceiveRoute>>>(null)

    val prefillWithClipboardWhenStartedFromLauncher by PrefKey.delegate(defaultValue = false)
    val prefillWithClipboardWhenStartedFromTile by PrefKey.delegate(defaultValue = doesSupportTiles)

    val closeDialogAfterSendWhenStartedFromLauncher by PrefKey.delegate(defaultValue = false)
    val closeDialogAfterSendWhenStartedFromSharesheet by PrefKey.delegate(defaultValue = true)
    val closeDialogAfterSendWhenStartedFromTile by PrefKey.delegate(defaultValue = true)

    val todoDraft by PrefKey.delegate(defaultValue = "")

    fun readSendReceiveRoutes(context: Context): InitializedLiveData<List<SendReceiveRoute>> {
        return accounts.get() ?: mutableLiveDataOf(
            context.readPref {
                val size = numberOfAccounts.value
                (0 until size).map { SendReceiveRoute.read(this@readPref, it)!! }
            }
        ).also {
            accounts = WeakReference(it)
        }
    }

    fun writeSendReceiveRoutes(context: Context, sendReceiveRoutes: List<SendReceiveRoute>) {
        require(sendReceiveRoutes.distinctBy { it.label }.size == sendReceiveRoutes.size)
        context.writePref {
            (0 until numberOfAccounts.value).forEach { SendReceiveRoute.write(this@writePref, it, null) }
            sendReceiveRoutes.forEachIndexed { index, account ->
                SendReceiveRoute.write(this@writePref, index, account)
            }
            numberOfAccounts.value = sendReceiveRoutes.size
        }
        PrefManager.accounts.get()?.value = sendReceiveRoutes
    }
}
