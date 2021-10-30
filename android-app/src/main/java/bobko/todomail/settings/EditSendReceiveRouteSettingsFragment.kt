package bobko.todomail.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import bobko.todomail.R
import bobko.todomail.model.SendReceiveRoute
import bobko.todomail.model.SmtpCredential
import bobko.todomail.model.pref.PrefManager
import bobko.todomail.util.*
import kotlin.reflect.KClass

private typealias SRR = SendReceiveRoute

class EditSendReceiveRouteSettingsFragment : Fragment() {
    val viewModel by viewModels<EditSendReceiveRouteSettingsFragmentViewModel>()
    fun parentActivity() = requireActivity() as SettingsActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = requireContext().composeView {
        val existingRoutes = PrefManager.readSendReceiveRoutes(requireContext()).value
        val mode =
            if (parentActivity().viewModel.sendReceiveRouteToEdit in existingRoutes) Mode.Edit
            else Mode.Add
        val existingLabels = existingRoutes.mapTo(mutableSetOf()) { it.label }
        EditSendReceiveRouteSettingsFragmentScreen(getSchema(existingLabels), mode)
    }
}

private enum class Mode {
    Edit, Add
}

private fun getSchema(existingLabels: MutableSet<String>): List<Item> {
    val smtpServerPortLens = SRR::credential.then { ::smtpServerPort }
    return listOf(
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
            smtpServerPortLens,
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
}

class EditSendReceiveRouteSettingsFragmentViewModel : ViewModel() {
    val sendReceiveRoute: MutableLiveData<SendReceiveRoute> = MutableLiveData()
    val showErrorIfFieldIsEmpty = mutableLiveDataOf(false)
}

private const val DEFAULT_SMTP_PORT = 25

fun suggestSendReceiveRouteLabel(context: Context): String {
    val existingLabels = PrefManager.readSendReceiveRoutes(context).value.mapTo(mutableSetOf()) { it.label }
    return sequenceOf("Todo", "Work")
        .plus(generateSequence(0) { it + 1 }.map { "Todo$it" })
        .first { it !in existingLabels }
}

@Composable
private fun EditSendReceiveRouteSettingsFragment.EditSendReceiveRouteSettingsFragmentScreen(
    schema: List<Item>,
    mode: Mode
) {
    SettingsScreen("Edit Send Receive Route Settings") {
        val sendReceiveRoute = viewModel.sendReceiveRoute.observeAsMutableState { // TODO add screen rotation test
            parentActivity().viewModel.sendReceiveRouteToEdit ?: SendReceiveRoute(
                suggestSendReceiveRouteLabel(requireContext()), "",
                SmtpCredential("", DEFAULT_SMTP_PORT, "", "")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        schema.forEach {
            it.Composable(schema, sendReceiveRoute, viewModel)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Buttons(schema, sendReceiveRoute, mode)
    }
}

@Composable
private fun EditSendReceiveRouteSettingsFragment.Buttons(
    schema: List<Item>,
    sendReceiveRoute: MutableState<SendReceiveRoute>,
    mode: Mode,
) {
    CenteredRow(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
        if (mode == Mode.Edit) {
            OutlinedButton(
                onClick = {
                    findNavController().navigateUp() // TODO
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                CenteredRow {
                    Icon(Icons.Rounded.Delete, "", tint = Color.Red)
                    Text("Delete", color = Color.Red)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Button(
            modifier = Modifier.weight(1f),
            onClick = {
                if (schema.filterIsInstance<Item.TextField<*>>().any { item ->
                        item.getCurrentText(sendReceiveRoute.value).let { it.isBlank() || item.errorProvider(it) != null }
                    }
                ) {
                    viewModel.showErrorIfFieldIsEmpty.value = true
                } else {
                    PrefManager.writeSendReceiveRoutes(
                        requireContext(),
                        PrefManager.readSendReceiveRoutes(requireContext()).value + listOf(sendReceiveRoute.value)
                    )
                    findNavController().navigateUp()
                }
            }
        ) {
            CenteredRow {
                when (mode) {
                    Mode.Edit -> {
                        Icon(Icons.Rounded.Done, "")
                        Text("Save")
                    }
                    Mode.Add -> {
                        Icon(Icons.Rounded.Add, "")
                        Text("Add")
                    }
                }
            }
        }
    }
}

private sealed class Item {
    data class TextField<T : Any>(
        val label: String,
        val lens: Lens<SendReceiveRoute, T>,
        val keyboardType: KeyboardType = KeyboardType.Text,
        val clazz: KClass<T> = String::class as KClass<T>,
        var focusRequester: FocusRequester? = null,
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
            schema: List<Item>,
            sendReceiveRoute: MutableState<SendReceiveRoute>,
            viewModel: EditSendReceiveRouteSettingsFragmentViewModel
        ) {
            val index = schema.indexOf(this)
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
                        imeAction = if (index == schema.lastIndex) ImeAction.Done else ImeAction.Next
                    ),
                    visualTransformation = run { // TODO IDE hadn't completed this parameter :( Need to fix in Kotlin plugin
                        if (keyboardType == KeyboardType.Password) PasswordVisualTransformation() else VisualTransformation.None
                    },
                    keyboardActions = KeyboardActions(
                        onNext = {
                            generateSequence(index + 1) { it + 1 }
                                .firstNotNullOfOrNull { schema.getOrNull(it)?.cast<TextField<*>>() }
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

    data class TextDivider(val text: String) : Item() {
        @Composable
        override fun Composable(
            schema: List<Item>,
            sendReceiveRoute: MutableState<SendReceiveRoute>,
            viewModel: EditSendReceiveRouteSettingsFragmentViewModel
        ) {
            bobko.todomail.settings.TextDivider(text = text)
        }
    }

    @Composable
    abstract fun Composable(
        schema: List<Item>,
        sendReceiveRoute: MutableState<SendReceiveRoute>,
        viewModel: EditSendReceiveRouteSettingsFragmentViewModel
    )
}
