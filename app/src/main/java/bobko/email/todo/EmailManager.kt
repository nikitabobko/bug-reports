package bobko.email.todo

import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailManager {

    fun sendEmailToMyself(subject: String, text: String) {
//        Thread.sleep(1000)
//        return
        val username = "foo@example.com"
        val password = ""

        val prop = Properties()
        prop["mail.smtp.host"] = "smtp.gmail.com"
        prop["mail.smtp.port"] = "587"
        prop["mail.smtp.auth"] = "true"
        prop["mail.smtp.starttls.enable"] = "true"

        val session = Session.getInstance(prop, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        val message = MimeMessage(session).apply {
            setRecipients(Message.RecipientType.TO, InternetAddress.parse("foo@example.com"))
            this.subject = subject
            setText(text)
        }

        Transport.send(message)
    }
}
