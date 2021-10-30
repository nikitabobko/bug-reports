package bobko.todomail

import bobko.todomail.model.SendReceiveRoute
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailManager {
    fun sendEmailToMyself(sendReceiveRoute: SendReceiveRoute, subject: String, body: String) {
        val prop = Properties()
        prop["mail.smtp.host"] = sendReceiveRoute.credential.smtpServer
        prop["mail.smtp.port"] = sendReceiveRoute.credential.smtpServerPort
        prop["mail.smtp.auth"] = "true"
        prop["mail.smtp.starttls.enable"] = "true"

        val session = Session.getInstance(prop, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(sendReceiveRoute.credential.username, sendReceiveRoute.credential.password)
            }
        })

        val message = MimeMessage(session).apply {
            setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(sendReceiveRoute.sendTo)
            )
            this.subject = subject
            setText(body)
        }

        Transport.send(message)
    }
}
