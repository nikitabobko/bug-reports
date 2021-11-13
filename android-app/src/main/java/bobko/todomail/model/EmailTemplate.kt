package bobko.todomail.model

import bobko.todomail.util.IndexedPrefKey
import bobko.todomail.util.PrefReaderContext
import bobko.todomail.util.PrefWriterContext

data class EmailTemplate(
    val label: String,
    val sendTo: String,
    val credential: SmtpCredential
) {
    companion object {
        private val emailTemplateLabel: IndexedPrefKey<String> by IndexedPrefKey.delegate()
        private val emailTemplateSendTo: IndexedPrefKey<String> by IndexedPrefKey.delegate()

        fun read(readerContext: PrefReaderContext, index: Int): EmailTemplate? =
            with(readerContext) {
                EmailTemplate(
                    emailTemplateLabel[index] ?: return null,
                    emailTemplateSendTo[index] ?: return null,
                    SmtpCredential.read(this, index) ?: return null
                )
            }

        fun write(writerContext: PrefWriterContext, index: Int, emailTemplate: EmailTemplate?) {
            with(writerContext) {
                emailTemplateLabel[index] = emailTemplate?.label
                emailTemplateSendTo[index] = emailTemplate?.sendTo
                SmtpCredential.write(this, index, emailTemplate?.credential)
            }
        }
    }
}
