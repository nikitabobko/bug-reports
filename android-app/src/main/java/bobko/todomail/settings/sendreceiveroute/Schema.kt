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
import bobko.todomail.settings.KnownSmtpCredential
import bobko.todomail.settings.emailIconSize
import bobko.todomail.util.*
import kotlin.reflect.KClass

fun getSchema(existingLabels: Set<String>) = listOf(
    Item.TextField(
        "Label",
        SendReceiveRoute::label.lens,
        String::class,
        errorProvider = { if (it in existingLabels) "Label '$it' already exist" else null }
    ),

    Item.TextDivider("Credentials settings"),
    Item.TextField(
        "SMTP Server",
        SendReceiveRoute::credential.then { ::smtpServer },
        String::class,
        rightSideHint = { sendReceiveRoute ->
            KnownSmtpCredential.findBySmtpServer(sendReceiveRoute)?.Icon()
        },
        isRightSideHintVisible = { sendReceiveRoute ->
            KnownSmtpCredential.findBySmtpServer(sendReceiveRoute) != null
        },
        onChanged = { route ->
            val smtpServerPortLens = SendReceiveRoute::credential.then { ::smtpServerPort }
            if (smtpServerPortLens.get(route.value) == DEFAULT_SMTP_PORT) {
                KnownSmtpCredential.findBySmtpServer(route.value)
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
        SendReceiveRoute::credential.then { ::smtpServerPort },
        Int::class,
        KeyboardType.Number,
        errorProvider = {
            val port = it.toIntOrNull() ?: 0
            when {
                port < 0 -> "SMTP Server Port cannot be negative"
                port > UShort.MAX_VALUE.toInt() -> "SMTP Server Port max possible value is ${UShort.MAX_VALUE.toInt()}"
                else -> null
            }
        },
        rightSideHint = { srr ->
            KnownSmtpCredential.findBySmtpServer(srr)
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
            KnownSmtpCredential.findBySmtpServer(srr)
                ?.let { srr.credential.smtpServerPort == it.smtpCredential.smtpServerPort } == true
        },
    ),
    Item.TextField("Username", SendReceiveRoute::credential.then { ::username }, String::class, KeyboardType.Email),
    Item.TextField(
        "Password",
        SendReceiveRoute::credential.then { ::password },
        String::class,
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
        SendReceiveRoute::sendTo.lens,
        String::class,
        KeyboardType.Email,
        rightSideHint = { sendReceiveRoute ->
            KnownSmtpCredential.findBySmtpServer(sendReceiveRoute)
                ?.suggestEmailSuffix(sendReceiveRoute.label)
                ?.let { suggestedEmailSuffix ->
                    OutlinedButton(onClick = { /*TODO*/ }, modifier = Modifier.height(56.dp)) {
                        Text(suggestedEmailSuffix)
                    }
                }
        },
        isRightSideHintVisible = { sendReceiveRoute ->
            sendReceiveRoute.sendTo.run { isNotBlank() && !contains("@") } &&
                    KnownSmtpCredential.findBySmtpServer(sendReceiveRoute) != null
        },
    ),
)

sealed class Item {
    class TextField<T : Any>(
        val label: String,
        private val lens: Lens<SendReceiveRoute, T>,
        private val clazz: KClass<T>,
        private val keyboardType: KeyboardType = KeyboardType.Text,
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
            val index = viewModel.schema.indexOf(this).also { check(it != -1) }
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
                        wasTextChangedManuallyAtLeastOnce = true
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
                    CenteredRow {
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            // OutlinedTextField has small label at top which makes centering a bit offseted to the bottom
                            Spacer(modifier = Modifier.size(8.dp))
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
