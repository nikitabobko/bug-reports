package bobko.todomail.model.pref

import android.content.Context
import bobko.todomail.model.EmailTemplate
import bobko.todomail.util.*
import kotlinx.coroutines.flow.*
import java.lang.ref.WeakReference

object PrefManager {
    private val numberOfAccounts by PrefKey.delegate(defaultValue = 0)
    private var accounts = WeakReference<MutableInitializedLiveData<List<EmailTemplate>>>(null)

    val prefillWithClipboardWhenStartedFromLauncher by PrefKey.delegate(defaultValue = false)
    val prefillWithClipboardWhenStartedFromTile by PrefKey.delegate(defaultValue = doesSupportTiles)

    val closeDialogAfterSendWhenStartedFromLauncher by PrefKey.delegate(defaultValue = false)
    val closeDialogAfterSendWhenStartedFromSharesheet by PrefKey.delegate(defaultValue = true)
    val closeDialogAfterSendWhenStartedFromTile by PrefKey.delegate(defaultValue = true)

    val todoDraft by PrefKey.delegate(defaultValue = "")

    fun readEmailTemplates(context: Context): InitializedLiveData<List<EmailTemplate>> {
        return accounts.get() ?: mutableLiveDataOf(
            context.readPref {
                val size = numberOfAccounts.value
                (0 until size).map { EmailTemplate.read(this@readPref, it)!! }
            }
        ).also {
            accounts = WeakReference(it)
        }
    }

    fun writeEmailTemplates(context: Context, emailTemplates: List<EmailTemplate>) {
        require(emailTemplates.distinctBy { it.label }.size == emailTemplates.size)
        context.writePref {
            (0 until numberOfAccounts.value).forEach { EmailTemplate.write(this@writePref, it, null) }
            emailTemplates.forEachIndexed { index, account ->
                EmailTemplate.write(this@writePref, index, account)
            }
            numberOfAccounts.value = emailTemplates.size
        }
        PrefManager.accounts.get()?.value = emailTemplates
    }
}
