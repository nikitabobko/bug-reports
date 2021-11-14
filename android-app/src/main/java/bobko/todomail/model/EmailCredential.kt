package bobko.todomail.model

import androidx.activity.ComponentActivity
import bobko.todomail.util.IndexedPrefKey
import bobko.todomail.util.PrefReaderContext
import bobko.todomail.util.PrefWriterContext

sealed class EmailCredential(val type: EmailCredentialType) {
    abstract fun sendEmail(activity: ComponentActivity, to: String, subject: String, body: String)

    companion object {
        private val credentialType: IndexedPrefKey<EmailCredentialType> by IndexedPrefKey.delegate()

        fun write(writerContext: PrefWriterContext, index: Int, value: EmailCredential?) {
            with (writerContext) {
                credentialType[index] =
            }
            when (value ?: return) {
                GoogleEmailCredential -> TODO()
                is SmtpCredential -> TODO()
            }
        }

        fun read(readContext: PrefReaderContext, index: Int): EmailCredential? {

        }
    }
}

enum class EmailCredentialType {
    Google, Smtp
}
