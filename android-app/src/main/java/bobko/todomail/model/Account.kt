package bobko.todomail.model

import bobko.todomail.util.IndexedPrefKey
import bobko.todomail.util.PrefReaderContext
import bobko.todomail.util.PrefWriterContext

data class Account(val label: String, val sendTo: String, val credential: SmtpCredential) {
    companion object {
        private val accountLabel: IndexedPrefKey<String> by IndexedPrefKey.delegate()
        private val accountSendTo: IndexedPrefKey<String> by IndexedPrefKey.delegate()

        fun read(readerContext: PrefReaderContext, index: Int): Account? =
            with(readerContext) {
                Account(
                    accountLabel[index] ?: return null,
                    accountSendTo[index] ?: return null,
                    SmtpCredential.read(this, index) ?: return null
                )
            }

        fun write(writerContext: PrefWriterContext, index: Int, account: Account?) {
            with(writerContext) {
                accountLabel[index] = account?.label
                accountSendTo[index] = account?.sendTo
                SmtpCredential.write(this, index, account?.credential)
            }
        }
    }
}
