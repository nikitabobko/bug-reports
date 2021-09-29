package bobko.email.todo

import android.app.Application
import androidx.preference.PreferenceManager
import bobko.email.todo.model.Account
import bobko.email.todo.util.*

object PrefManager {
    private val numberOfAccounts by PrefKey.delegate(defaultValue = 0)

    private var accounts: NotNullableMutableLiveData<SizedSequence<Account>>? = null

    fun readAccounts(application: Application): NotNullableLiveData<SizedSequence<Account>> {
        return accounts ?: NotNullableMutableLiveData(
            PreferenceManager.getDefaultSharedPreferences(application).read {
                val size = numberOfAccounts.value
                SizedSequence(
                    (0 until size).asSequence().map { Account.read(this@read, it)!! },
                    size
                )
            }
        ).also {
            accounts = it
        }
    }

    fun writeAccounts(application: Application, accounts: List<Account>) {
        this@PrefManager.accounts?.value = SizedSequence(accounts.asSequence(), accounts.size)
        require(accounts.distinctBy { it.label }.size == accounts.size)
        PreferenceManager.getDefaultSharedPreferences(application).write {
            (0 until numberOfAccounts.value).map { Account.write(this@write, it, null) }
            accounts.forEachIndexed { index, smtpCredentials ->
                Account.write(this@write, index, smtpCredentials)
            }
            numberOfAccounts.value = accounts.size
        }
    }
}
