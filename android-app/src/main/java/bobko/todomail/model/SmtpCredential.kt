package bobko.todomail.model

import bobko.todomail.util.IndexedPrefKey
import bobko.todomail.util.PrefReaderContext
import bobko.todomail.util.PrefWriterContext

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

        fun read(readContext: PrefReaderContext, index: Int): SmtpCredential? =
            with(readContext) {
                SmtpCredential(
                    smtpServer[index] ?: return null,
                    smtpServerPort[index] ?: return null,
                    smtpUsername[index] ?: return null,
                    smtpPassword[index] ?: return null,
                )
            }

        fun write(writerContext: PrefWriterContext, index: Int, value: SmtpCredential?) {
            with(writerContext) {
                smtpServer[index] = value?.smtpServer
                smtpServerPort[index] = value?.smtpServerPort
                smtpUsername[index] = value?.username
                smtpPassword[index] = value?.password
            }
        }
    }
}
