package bobko.todomail.settings

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import bobko.todomail.R
import bobko.todomail.model.SendReceiveRoute
import bobko.todomail.model.SmtpCredential
import bobko.todomail.model.pref.PrefManager

class SettingsActivity : AppCompatActivity() {
    val viewModel: SettingsActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }
}

data class SmtpCredentialWithMetaInfo(
    val label: String,
    @DrawableRes val iconResId: Int,
    val smtpCredential: SmtpCredential,
    val domain: String,
    val suggestEmailSuffix: (currentLabel: String) -> String = { "@$domain" },
) {
    @Composable
    fun Icon() {
        androidx.compose.material.Icon(
            painterResource(id = iconResId),
            contentDescription = label,
            modifier = Modifier.size(emailIconSize),
            tint = Color.Unspecified
        )
    }
}

val emailIconSize = 32.dp

val knownSmtpCredentials = listOf(
    SmtpCredentialWithMetaInfo(
        "Gmail (SMTP)",
        R.drawable.ic_gmail_icon,
        SmtpCredential("smtp.gmail.com", 587, username = "", password = ""),
        domain = "gmail.com",
        suggestEmailSuffix = { currentText -> "+${currentText.lowercase()}@gmail.com" },
    ),
    SmtpCredentialWithMetaInfo(
        "Outlook (SMTP)",
        R.drawable.outlook_icon,
        SmtpCredential("smtp-mail.outlook.com", 587, username = "", password = ""),
        domain = "outlook.com",
    ),
    SmtpCredentialWithMetaInfo(
        "Yahoo Mail (SMTP)",
        R.drawable.yahoo_mail,
        SmtpCredential("smtp.mail.yahoo.com", 465, username = "", password = ""),
        domain = "yahoo.com",
    ),
)

fun List<SmtpCredentialWithMetaInfo>.findBySmtpServer(smtpServer: String): SmtpCredentialWithMetaInfo? =
    singleOrNull { it.smtpCredential.smtpServer == smtpServer }

class SettingsActivityViewModel : ViewModel() {
    var sendReceiveRouteToEdit: SendReceiveRoute? = null
}
