package bobko.email.todo.model

import bobko.email.todo.util.IndexedPrefKey
import bobko.email.todo.util.PrefReaderContext
import bobko.email.todo.util.PrefWriterContext

data class SmtpCredential(
    val smtpServer: String,
    val smtpServerPort: Int,
    val username: String,
    val password: String
) {
    companion object {
        private val smtpServer: IndexedPrefKey<String> by IndexedPrefKey.delegate()
        private val smtpServerPort: IndexedPrefKey<Int> by IndexedPrefKey.delegate()
        private val smtpUsername: IndexedPrefKey<String> by IndexedPrefKey.delegate()
        private val smtpPassword: IndexedPrefKey<String> by IndexedPrefKey.delegate()

        fun read(readerContext: PrefReaderContext, index: Int): SmtpCredential? =
            with(readerContext) {
                SmtpCredential(
                    smtpServer[index] ?: return null,
                    smtpServerPort[index] ?: return null,
                    smtpUsername[index] ?: return null,
                    smtpPassword[index] ?: return null,
                )
            }

        fun write(
            prefWriterContext: PrefWriterContext,
            index: Int,
            value: SmtpCredential?
        ) {
            with(prefWriterContext) {
                smtpServer[index] = value?.smtpServer
                smtpServerPort[index] = value?.smtpServerPort
                smtpUsername[index] = value?.username
                smtpPassword[index] = value?.password
            }
        }
    }
}
