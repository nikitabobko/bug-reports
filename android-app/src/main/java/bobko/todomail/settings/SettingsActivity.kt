package bobko.todomail.settings

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
import bobko.todomail.model.EmailTemplate
import bobko.todomail.model.SmtpCredential

class SettingsActivity : AppCompatActivity() {
    val viewModel: SettingsActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }
}

enum class KnownSmtpCredential(
    val label: String,
    @DrawableRes val iconResId: Int,
    val smtpCredential: SmtpCredential,
    val domain: String,
) {
    Gmail(
        "Gmail (SMTP)",
        R.drawable.ic_gmail_icon,
        SmtpCredential("smtp.gmail.com", 587, username = "", password = ""),
        domain = "gmail.com"
    ) {
        override fun suggestEmailSuffix(currentLabel: String): String {
            return "+${currentLabel.lowercase()}@gmail.com"
        }
    },
    Outlook(
        "Outlook (SMTP)",
        R.drawable.outlook_icon,
        SmtpCredential("smtp-mail.outlook.com", 587, username = "", password = ""),
        domain = "outlook.com",
    ),
    Yahoo(
        "Yahoo Mail (SMTP)",
        R.drawable.yahoo_mail,
        SmtpCredential("smtp.mail.yahoo.com", 465, username = "", password = ""),
        domain = "yahoo.com",
    );

    @Composable
    fun Icon() {
        androidx.compose.material.Icon(
            painterResource(id = iconResId),
            contentDescription = label,
            modifier = Modifier.size(emailIconSize),
            tint = Color.Unspecified
        )
    }

    open fun suggestEmailSuffix(currentLabel: String) = "@$domain"

    companion object {
        fun findBySmtpServer(route: EmailTemplate): KnownSmtpCredential? =
            values().singleOrNull { it.smtpCredential.smtpServer == route.credential.smtpServer }
    }
}

val emailIconSize = 32.dp

class SettingsActivityViewModel : ViewModel() {
    var emailTemplateToEdit: EmailTemplate? = null
}
