package bobko.email.todo

import bobko.email.todo.model.Account
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailManager {
    fun sendEmailToMyself(account: Account, subject: String, body: String) {
        val prop = Properties()
        prop["mail.smtp.host"] = account.credential.smtpServer
        prop["mail.smtp.port"] = account.credential.smtpServerPort
        prop["mail.smtp.auth"] = "true"
        prop["mail.smtp.starttls.enable"] = "true"

        val session = Session.getInstance(prop, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(account.credential.username, account.credential.password)
            }
        })

        val message = MimeMessage(session).apply {
            setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(account.sendTo)
            )
            this.subject = subject
            setText(body)
        }

        Transport.send(message)
    }
}
