package bobko.email.todo

import android.content.Context
import android.content.SharedPreferences
import bobko.email.todo.model.Account
import bobko.email.todo.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

object PrefManager {
    private val numberOfAccounts by PrefKey.delegate(defaultValue = 0)
    private var accounts = WeakReference<NotNullableMutableLiveData<List<Account>>>(null)

    val prefillWithClipboardWhenStartedFromLauncher by PrefKey.delegate(defaultValue = false)
    val prefillWithClipboardWhenStartedFromTile by PrefKey.delegate(defaultValue = doesSupportTiles)

    val appendAppNameThatSharedTheText by PrefKey.delegate(defaultValue = true)

    val closeDialogAfterSendWhenStartedFromLauncher by PrefKey.delegate(defaultValue = false)
    val closeDialogAfterSendWhenStartedFromSharesheet by PrefKey.delegate(defaultValue = true)
    val closeDialogAfterSendWhenStartedFromTile by PrefKey.delegate(defaultValue = true)

    fun readAccounts(context: Context): NotNullableLiveData<List<Account>> {
        return accounts.get() ?: NotNullableMutableLiveData(
            context.readPref {
                val size = numberOfAccounts.value
                (0 until size).asSequence().map { Account.read(this@readPref, it)!! }.toList()
            }
        ).also {
            accounts = WeakReference(it)
        }
    }

    fun writeAccounts(context: Context, accounts: List<Account>) {
        require(accounts.distinctBy { it.label }.size == accounts.size)
        context.writePref {
            (0 until numberOfAccounts.value).forEach { Account.write(this@writePref, it, null) }
            accounts.forEachIndexed { index, account ->
                Account.write(this@writePref, index, account)
            }
            numberOfAccounts.value = accounts.size
        }
        this@PrefManager.accounts.get()?.value = accounts
    }
}
