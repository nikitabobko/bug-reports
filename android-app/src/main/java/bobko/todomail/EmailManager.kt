package bobko.todomail

import bobko.todomail.model.EmailTemplate
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailManager {
    fun sendEmailToMyself(emailTemplate: EmailTemplate, subject: String, body: String) {
        val prop = Properties()
        prop["mail.smtp.host"] = emailTemplate.credential.smtpServer
        prop["mail.smtp.port"] = emailTemplate.credential.smtpServerPort
        prop["mail.smtp.auth"] = "true"
        prop["mail.smtp.starttls.enable"] = "true"

        val session = Session.getInstance(prop, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(emailTemplate.credential.username, emailTemplate.credential.password)
            }
        })

        val message = MimeMessage(session).apply {
            setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(emailTemplate.sendTo)
            )
            this.subject = subject
            setText(body)
        }

        Transport.send(message)
    }
}
