package bobko.todomail.login

import android.util.Base64
import bobko.todomail.util.errorException
import com.github.kittinunf.fuel.core.ResponseResultOf
import com.github.kittinunf.fuel.core.isSuccessful
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.jackson.jacksonDeserializerOf
import com.github.kittinunf.fuel.jackson.objectBody
import com.github.kittinunf.result.Result
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import java.io.ByteArrayOutputStream
import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


// https://developers.google.com/gmail/api/guides/sending

fun createEmail(to: String?, from: String?, subject: String?, bodyText: String?): MimeMessage {
    val props = Properties()
    val session: Session = Session.getDefaultInstance(props, null)
    val email = MimeMessage(session)
    email.addRecipient(
        Message.RecipientType.TO,
        InternetAddress(to)
    )
    email.subject = subject
    email.setText(bodyText)
    return email
}

data class GoogleOauth2TokenResponse(val access_token: String)
data class GmailMessageSendRequest(val raw: String)

val <T> ResponseResultOf<T>.value: T
    get() {
        val (request, response, result) = this
        if (!response.isSuccessful) {
            error(
                """
                Request failed with ${response.statusCode}
                request=$request
                response=$response
            """.trimIndent()
            )
        }
        return when (result) {
            is Result.Success -> {
                result.value
            }
            is Result.Failure -> {
                errorException(result.getException())
            }
        }
    }

fun sendItSuka(account: GoogleSignInAccount) {

    val buffer = ByteArrayOutputStream()
    createEmail(
        "nikitabobko@gmail.com",
        "nikitabobko@gmail.com",
        "Foo",
        "а нука сука держи русский"
    ).writeTo(buffer)

    val raw = Base64.encodeToString(
        buffer.toByteArray(),
        Base64.NO_WRAP
    )

    val accessToken = "https://accounts.google.com/o/oauth2/token"
        .httpPost(
            parameters = listOf(
                "client_id" to "473994673878-hpbjfm51euanc0molpthbesm82u3eatl.apps.googleusercontent.com",
                "client_secret" to "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",
                "grant_type" to "authorization_code",
                "code" to account.serverAuthCode!!
            )
        )
        .response(jacksonDeserializerOf<GoogleOauth2TokenResponse>())
        .value
        .access_token

    // https://developers.google.com/gmail/api/guides/sending
    // https://developers.google.com/gmail/api/reference/rest/v1/users.messages/send
    "https://gmail.googleapis.com/gmail/v1/users/me/messages/send"
        .httpPost()
        .header(
            "Authorization" to "Bearer $accessToken",
            "Accept" to "application/json",
            "Content-Type" to "application/json"
        )
        .objectBody(GmailMessageSendRequest(raw))
        .responseString()
        .value
}
