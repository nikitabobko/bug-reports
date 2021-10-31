package bobko.todomail.settings.sendreceiveroute

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import bobko.todomail.model.SendReceiveRoute
import bobko.todomail.settings.emailIconSize
import bobko.todomail.settings.findBySmtpServer
import bobko.todomail.settings.knownSmtpCredentials
import bobko.todomail.util.*
import kotlin.reflect.KClass

private typealias SRR = SendReceiveRoute

fun getSchema(existingLabels: Set<String>) = listOf(
    Item.TextField(
        "Label",
        SRR::label.lens,
        errorProvider = { if (it in existingLabels) "Label '$it' already exist" else null }
    ),

    Item.TextDivider("Credentials settings"),
    Item.TextField(
        "SMTP Server",
        SRR::credential.then { ::smtpServer },
        rightSideHint = { sendReceiveRoute ->
            knownSmtpCredentials.findBySmtpServer(sendReceiveRoute.credential.smtpServer)?.Icon()
        },
        isRightSideHintVisible = { sendReceiveRoute ->
            knownSmtpCredentials.findBySmtpServer(sendReceiveRoute.credential.smtpServer) != null
        },
        onChanged = { route ->
            val smtpServerPortLens = SRR::credential.then { ::smtpServerPort }
            if (smtpServerPortLens.get(route.value) == DEFAULT_SMTP_PORT) {
                knownSmtpCredentials.findBySmtpServer(route.value.credential.smtpServer)
                    ?.smtpCredential
                    ?.smtpServerPort
                    ?.let {
                        route.value = smtpServerPortLens.set(route.value, it)
                    }
            }
        }
    ),
    Item.TextField(
        "SMTP Server Port",
        SRR::credential.then { ::smtpServerPort },
        KeyboardType.Number,
        Int::class,
        errorProvider = {
            val port = it.toIntOrNull() ?: 0
            when {
                port < 0 -> "SMTP Server Port cannot be negative"
                port > UShort.MAX_VALUE.toInt() -> "SMTP Server Port max possible value is ${UShort.MAX_VALUE.toInt()}"
                else -> null
            }
        },
        rightSideHint = { srr ->
            knownSmtpCredentials.findBySmtpServer(srr.credential.smtpServer)
                ?.takeIf { srr.credential.smtpServerPort == it.smtpCredential.smtpServerPort }
                ?.let {
                    Icon(
                        painterResource(id = R.drawable.verified_icon_24),
                        "",
                        modifier = Modifier.size(emailIconSize),
                        tint = MaterialTheme.colors.primary
                    )
                }
        },
        isRightSideHintVisible = { srr ->
            knownSmtpCredentials.findBySmtpServer(srr.credential.smtpServer)
                ?.let { srr.credential.smtpServerPort == it.smtpCredential.smtpServerPort } == true
        },
    ),
    Item.TextField("Username", SRR::credential.then { ::username }, KeyboardType.Email),
    Item.TextField(
        "Password",
        SRR::credential.then { ::password },
        KeyboardType.Password,
        rightSideHint = {
            IconButton(onClick = { /*TODO*/ }, modifier = Modifier.size(emailIconSize)) {
                Icon(
                    painterResource(id = R.drawable.visibility_on_icon_24),
                    "",
                    modifier = Modifier.size(emailIconSize),
                    tint = MaterialTheme.colors.primary
                )
            }
        },
        isRightSideHintVisible = { true }
    ),

    Item.TextDivider("Destination address settings"),
    Item.TextField(
        "Send to",
        SRR::sendTo.lens,
        KeyboardType.Email,
        rightSideHint = { sendReceiveRoute ->
            knownSmtpCredentials.findBySmtpServer(sendReceiveRoute.credential.smtpServer)
                ?.suggestEmailSuffix
                ?.invoke(sendReceiveRoute.label)
                ?.let { suggestedEmailSuffix ->
                    OutlinedButton(onClick = { /*TODO*/ }) {
                        Text(suggestedEmailSuffix)
                    }
                }
        },
        isRightSideHintVisible = { sendReceiveRoute ->
            !sendReceiveRoute.sendTo.contains("@") &&
                    knownSmtpCredentials.findBySmtpServer(sendReceiveRoute.credential.smtpServer) != null
        },
    ),
)

sealed class Item {
    class TextField<T : Any>(
        val label: String,
        private val lens: Lens<SendReceiveRoute, T>,
        private val keyboardType: KeyboardType = KeyboardType.Text,
        private val clazz: KClass<T> = String::class as KClass<T>,
        var focusRequester: FocusRequester? = null,
        var wasTextChangedManuallyAtLeastOnce: Boolean = false,
        val errorProvider: (currentText: String) -> String? = { null },
        val rightSideHint: @Composable (SendReceiveRoute) -> Unit = { },
        val isRightSideHintVisible: (SendReceiveRoute) -> Boolean = { false },
        val onChanged: (MutableState<SendReceiveRoute>) -> Unit = {}
    ) : Item() {
        init {
            check(clazz == Int::class || clazz == String::class)
        }

        fun getCurrentText(sendReceiveRoute: SendReceiveRoute): String {
            return lens.get(sendReceiveRoute).takeIf { it != -1 }?.toString() ?: ""
        }

        @OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
        @Composable
        override fun Composable(
            sendReceiveRoute: MutableState<SendReceiveRoute>,
            viewModel: EditSendReceiveRouteSettingsFragmentViewModel
        ) {
            val index = viewModel.schema.indexOf(this)
            val focusRequester = remember { FocusRequester() }
            this.focusRequester = focusRequester
            val keyboard = LocalSoftwareKeyboardController.current

            val currentText = getCurrentText(sendReceiveRoute.value)
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
                    visualTransformation = run { // TODO IDE hadn't completed this parameter :( Need to fix in Kotlin plugin
                        if (keyboardType == KeyboardType.Password) PasswordVisualTransformation() else VisualTransformation.None
                    },
                    keyboardActions = KeyboardActions(
                        onNext = {
                            generateSequence(index + 1) { it + 1 }
                                .firstNotNullOfOrNull { viewModel.schema.getOrNull(it)?.cast<TextField<*>>() }
                                ?.focusRequester
                                ?.requestFocus()
                        },
                        onDone = {
                            keyboard?.hide()
                        }
                    ),
                    onValueChange = { newValueRaw ->
                        val newValue = when (clazz) {
                            // FYI https://issuetracker.google.com/issues/204522152
                            Int::class -> if (newValueRaw.isEmpty()) -1 else newValueRaw.toIntOrNull()
                            String::class -> newValueRaw
                            else -> error("")
                        } as? T?
                        newValue?.let {
                            sendReceiveRoute.value = lens.set(sendReceiveRoute.value, it)
                        }
                        onChanged(sendReceiveRoute)
                    },
                )
                AnimatedVisibility(visible = isRightSideHintVisible(sendReceiveRoute.value)) {
                    Column {
                        // OutlinedTextField has small label at top which makes centering a bit offseted to the bottom
                        Spacer(modifier = Modifier.size(8.dp))
                        CenteredRow {
                            Spacer(modifier = Modifier.width(16.dp))
                            rightSideHint(sendReceiveRoute.value)
                        }
                    }
                }
            }
        }
    }

    class TextDivider(val text: String) : Item() {
        @Composable
        override fun Composable(
            sendReceiveRoute: MutableState<SendReceiveRoute>,
            viewModel: EditSendReceiveRouteSettingsFragmentViewModel
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            bobko.todomail.settings.TextDivider(text = text)
        }
    }

    @Composable
    abstract fun Composable(
        sendReceiveRoute: MutableState<SendReceiveRoute>,
        viewModel: EditSendReceiveRouteSettingsFragmentViewModel
    )
}
