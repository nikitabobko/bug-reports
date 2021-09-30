package bobko.email.todo

import android.app.Application
import androidx.preference.PreferenceManager
import bobko.email.todo.model.Account
import bobko.email.todo.util.*

object PrefManager {
    private val numberOfAccounts by PrefKey.delegate(defaultValue = 0)

    private var accounts: NotNullableMutableLiveData<List<Account>>? = null

    fun readAccounts(application: Application /*TODO do I really need Application here?*/): NotNullableLiveData<List<Account>> {
        return accounts ?: NotNullableMutableLiveData(
            PreferenceManager.getDefaultSharedPreferences(application).read {
                val size = numberOfAccounts.value
                (0 until size).asSequence().map { Account.read(this@read, it)!! }.toList()
            }
        ).also {
            accounts = it
        }
    }

    fun writeAccounts(application: Application, accounts: List<Account>) {
        this@PrefManager.accounts?.value = accounts
        require(accounts.distinctBy { it.label }.size == accounts.size)
        PreferenceManager.getDefaultSharedPreferences(application).write {
            (0 until numberOfAccounts.value).forEach { Account.write(this@write, it, null) }
            accounts.forEachIndexed { index, account ->
                Account.write(this@write, index, account)
            }
            numberOfAccounts.value = accounts.size
        }
    }
}
