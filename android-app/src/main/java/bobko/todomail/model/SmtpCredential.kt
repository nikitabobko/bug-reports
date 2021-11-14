package bobko.todomail.model

import androidx.activity.ComponentActivity
import bobko.todomail.util.IndexedPrefKey
import bobko.todomail.util.PrefReaderContext
import bobko.todomail.util.PrefWriterContext
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

data class SmtpCredential(
    val smtpServer: String,
    val smtpServerPort: Int,
    val username: String,
    val password: String
) : EmailCredential() {
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

    override fun sendEmail(activity: ComponentActivity, to: String, subject: String, body: String) {
        val prop = Properties()
        prop["mail.smtp.host"] = smtpServer
        prop["mail.smtp.port"] = smtpServerPort
        prop["mail.smtp.auth"] = "true"
        prop["mail.smtp.starttls.enable"] = "true"

        val session = Session.getInstance(prop, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        val message = MimeMessage(session).apply {
            setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(to)
            )
            this.subject = subject
            setText(body)
        }

        Transport.send(message)
    }
}
