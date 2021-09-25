package bobko.email.todo.model

import bobko.email.todo.util.IndexedPrefKey
import bobko.email.todo.util.PrefReaderContext
import bobko.email.todo.util.PrefWriterContext

data class Account(val label: String, val sendTo: String, val credential: SmtpCredential) {
    companion object {
        private val accountLabel: IndexedPrefKey<String> by IndexedPrefKey.delegate()
        private val accountSendTo: IndexedPrefKey<String> by IndexedPrefKey.delegate()

        fun read(prefReaderContext: PrefReaderContext, index: Int): Account? =
            with(prefReaderContext) {
                Account(
                    accountLabel[index] ?: return null,
                    accountSendTo[index] ?: return null,
                    SmtpCredential.read(this, index) ?: return null
                )
            }

        fun write(prefWriterContext: PrefWriterContext, index: Int, account: Account?) {
            with(prefWriterContext) {
                accountLabel[index] = account?.label
                accountSendTo[index] = account?.sendTo
                SmtpCredential.write(this, index, account?.credential)
            }
        }
    }
}
