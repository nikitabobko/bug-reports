package bobko.todomail.settings.emailtemplate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import bobko.todomail.R
import bobko.todomail.model.EmailTemplate
import bobko.todomail.settings.KnownSmtpCredential
import bobko.todomail.settings.emailIconSize
import bobko.todomail.util.*
import kotlin.reflect.KClass

fun getSchema(existingLabels: Set<String>) = listOf(
    LabelTextFieldItem(existingLabels),

    TextDivider("Credentials settings"),
    SmtpServerTextFieldItem(),
    SmtpServerPortTextFieldItem(),
    UsernameTextFieldItem(),
    PasswordTextFieldItem(),

    TextDivider("Destination address settings"),
    SendToTextFieldItem(),
)

sealed class Item {
    @Composable
    abstract fun Composable(
        emailTemplate: MutableState<EmailTemplate>,
        viewModel: EditEmailTemplateSettingsFragmentViewModel
    )
}

class TextDivider(val text: String) : Item() {
    @Composable
    override fun Composable(
        emailTemplate: MutableState<EmailTemplate>,
        viewModel: EditEmailTemplateSettingsFragmentViewModel
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        bobko.todomail.settings.TextDivider(text = text)
    }
}

// Don't try to make it virtual instead of extension function :) https://issuetracker.google.com/issues/204650732
@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
private fun <T : Any> TextFieldItem<T>.TextFieldComposable(
    emailTemplate: MutableState<EmailTemplate>,
    viewModel: EditEmailTemplateSettingsFragmentViewModel
) {
    val index = viewModel.schema.indexOf(this).also { check(it != -1) }
    val focusRequester = remember { FocusRequester() }
    this.focusRequester = focusRequester
    val keyboard = LocalSoftwareKeyboardController.current

    val currentText = getCurrentText(emailTemplate.value)
    val showErrorIfFieldIsEmpty by viewModel.showErrorIfFieldIsEmpty.observeAsState()
    val error = errorProvider(currentText) ?: label.takeIf { showErrorIfFieldIsEmpty && currentText.isBlank() }

    CenteredRow(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
        OutlinedTextField(
            value = currentText,
            label = { Text(error ?: label) },
            isError = error != null,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = if (index == viewModel.schema.lastIndex) ImeAction.Done else ImeAction.Next
            ),
            // TODO IDE hadn't completed this parameter :( Need to fix in Kotlin plugin
            visualTransformation = textFieldVisualTransformation.value,
            keyboardActions = KeyboardActions(
                onNext = {
                    generateSequence(index + 1) { it + 1 }
                        .firstNotNullOfOrNull { viewModel.schema.getOrNull(it)?.cast<TextFieldItem<*>>() }
                        ?.focusRequester
                        ?.requestFocus()
                },
                onDone = {
                    keyboard?.hide()
                }
            ),
            onValueChange = { newValueRaw ->
                wasTextChangedManuallyAtLeastOnce = true
                val newValue = when (clazz) {
                    // FYI https://issuetracker.google.com/issues/204522152
                    Int::class -> if (newValueRaw.isEmpty()) -1 else newValueRaw.toIntOrNull()
                    String::class -> newValueRaw
                    else -> error("")
                } as? T?
                newValue?.let {
                    emailTemplate.value = lens.set(emailTemplate.value, it)
                }
                onTextChanged(emailTemplate)
            },
        )
        AnimatedVisibility(visible = isRightSideHintVisible(emailTemplate.value)) {
            CenteredRow {
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    // OutlinedTextField has small label at top which makes centering a bit offseted to the bottom
                    Spacer(modifier = Modifier.size(8.dp))
                    RightSideHint(emailTemplate.value)
                }
            }
        }
    }
}

sealed class TextFieldItem<T : Any>(
    val label: String,
    val lens: Lens<EmailTemplate, T>,
    val clazz: KClass<T>,
    val keyboardType: KeyboardType = KeyboardType.Text,
    var focusRequester: FocusRequester? = null,
    var wasTextChangedManuallyAtLeastOnce: Boolean = false,
) : Item() {
    init {
        require(clazz == Int::class || clazz == String::class)
    }

    fun getCurrentText(emailTemplate: EmailTemplate): String {
        return lens.get(emailTemplate).takeIf { it != -1 }?.toString() ?: ""
    }

    open fun errorProvider(currentText: String): String? = null
    open fun onTextChanged(emailTemplate: MutableState<EmailTemplate>) {}
    open fun isRightSideHintVisible(emailTemplate: EmailTemplate) = false

    @Composable
    open fun RightSideHint(emailTemplate: EmailTemplate) {
    }

    open val textFieldVisualTransformation: State<VisualTransformation> =
        mutableStateOf(VisualTransformation.None)

    @Composable
    override fun Composable(
        emailTemplate: MutableState<EmailTemplate>,
        viewModel: EditEmailTemplateSettingsFragmentViewModel
    ) {
        TextFieldComposable(emailTemplate, viewModel)
    }
}

class LabelTextFieldItem(private val existingLabels: Set<String>) : TextFieldItem<String>(
    "Label",
    EmailTemplate::label.lens,
    String::class
) {
    override fun errorProvider(currentText: String) =
        if (currentText in existingLabels) "Label '$currentText' already exist" else null
}

class SmtpServerTextFieldItem() : TextFieldItem<String>(
    "SMTP Server",
    EmailTemplate::credential.then { ::smtpServer },
    String::class,
) {
    override fun onTextChanged(emailTemplate: MutableState<EmailTemplate>) {
        val smtpServerPortLens = EmailTemplate::credential.then { ::smtpServerPort }
        if (smtpServerPortLens.get(emailTemplate.value) == DEFAULT_SMTP_PORT) {
            KnownSmtpCredential.findBySmtpServer(emailTemplate.value)
                ?.smtpCredential
                ?.smtpServerPort
                ?.let {
                    emailTemplate.value = smtpServerPortLens.set(emailTemplate.value, it)
                }
        }
    }

    override fun isRightSideHintVisible(emailTemplate: EmailTemplate) =
        KnownSmtpCredential.findBySmtpServer(emailTemplate) != null

    // TODO report that Kotlin plugin doesn't complete @Composable method override
    @Composable
    override fun RightSideHint(emailTemplate: EmailTemplate) {
        KnownSmtpCredential.findBySmtpServer(emailTemplate)?.Icon()
    }
}

class SmtpServerPortTextFieldItem() : TextFieldItem<Int>(
    "SMTP Server Port",
    EmailTemplate::credential.then { ::smtpServerPort },
    Int::class,
    KeyboardType.Number,
) {
    override fun errorProvider(currentText: String): String? {
        val port = currentText.toIntOrNull() ?: 0
        return when {
            port < 0 -> "SMTP Server Port cannot be negative"
            port > UShort.MAX_VALUE.toInt() -> "SMTP Server Port max possible value is ${UShort.MAX_VALUE.toInt()}"
            else -> null
        }
    }

    private fun getKnownSmtpServerPortPair(emailTemplate: EmailTemplate): KnownSmtpCredential? =
        KnownSmtpCredential.findBySmtpServer(emailTemplate)
            ?.takeIf { emailTemplate.credential.smtpServerPort == it.smtpCredential.smtpServerPort }

    override fun isRightSideHintVisible(emailTemplate: EmailTemplate) =
        getKnownSmtpServerPortPair(emailTemplate) != null

    @Composable
    override fun RightSideHint(emailTemplate: EmailTemplate) {
        getKnownSmtpServerPortPair(emailTemplate)?.let {
            Icon(
                painterResource(id = R.drawable.verified_icon_24),
                "",
                modifier = Modifier.size(emailIconSize),
                tint = MaterialTheme.colors.primary
            )
        }
    }
}

class PasswordTextFieldItem() : TextFieldItem<String>(
    "Password",
    EmailTemplate::credential.then { ::password },
    String::class,
    KeyboardType.Password,
) {
    override val textFieldVisualTransformation: MutableState<VisualTransformation> =
        mutableStateOf(PasswordVisualTransformation())

    @Composable
    override fun Composable(
        emailTemplate: MutableState<EmailTemplate>,
        viewModel: EditEmailTemplateSettingsFragmentViewModel
    ) {
        Column {
            TextFieldComposable(emailTemplate, viewModel)
            Spacer(modifier = Modifier.height(8.dp))
            val onClick = {
                textFieldVisualTransformation.value =
                    if (textFieldVisualTransformation.value is PasswordVisualTransformation) VisualTransformation.None
                    else PasswordVisualTransformation()
            }
            CenteredRow(modifier = Modifier.padding(start = 16.dp)) {
                Checkbox(
                    checked = textFieldVisualTransformation.value !is PasswordVisualTransformation,
                    onCheckedChange = { onClick() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Show password",
                    modifier = Modifier.clickable(onClick = onClick)
                )
            }
        }
    }
}

class UsernameTextFieldItem() : TextFieldItem<String>(
    "Username",
    EmailTemplate::credential.then { ::username },
    String::class,
    KeyboardType.Email
)

class SendToTextFieldItem() : TextFieldItem<String>(
    "Send to",
    EmailTemplate::sendTo.lens,
    String::class,
    KeyboardType.Email,
) {
    override fun isRightSideHintVisible(emailTemplate: EmailTemplate) =
        emailTemplate.sendTo.run { isNotBlank() && !contains("@") } &&
                KnownSmtpCredential.findBySmtpServer(emailTemplate) != null

    @Composable
    override fun RightSideHint(emailTemplate: EmailTemplate) {
        KnownSmtpCredential.findBySmtpServer(emailTemplate)
            ?.suggestEmailSuffix(emailTemplate.label)
            ?.let { suggestedEmailSuffix ->
                OutlinedButton(onClick = { /*TODO*/ }, modifier = Modifier.height(56.dp)) {
                    Text(suggestedEmailSuffix)
                }
            }
    }
}
