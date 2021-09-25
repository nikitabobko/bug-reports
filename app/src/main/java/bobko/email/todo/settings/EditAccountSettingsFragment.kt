package bobko.email.todo.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import bobko.email.todo.PrefManager
import bobko.email.todo.model.Account
import bobko.email.todo.model.SmtpCredential
import bobko.email.todo.ui.theme.EmailTodoTheme
import bobko.email.todo.util.composeView
import bobko.email.todo.util.observeAsMutableState
import bobko.email.todo.util.orElse

class EditAccountSettingsFragment : DialogFragment() {
    fun parentActivity() = requireActivity() as SettingsActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = requireContext().composeView {
        EditAccountSettingsFragmentScreen()
    }
}

@Composable
fun EditAccountSettingsFragment.EditAccountSettingsFragmentScreen() {
    EmailTodoTheme {
        Surface {
            Column(modifier = Modifier.padding(8.dp)) {
                val account by parentActivity().viewModel.accountTemplate.observeAsMutableState()
                var smtpServer by remember {
                    mutableStateOf(
                        account?.credential?.smtpServer ?: ""
                    )
                }
                var smtpServerPort by remember {
                    mutableStateOf(
                        account?.credential?.smtpServerPort?.toString() ?: ""
                    )
                }
                var label by remember { mutableStateOf(account?.label ?: "") }
                var username by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var sendTo by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = label,
                    label = { Text("Label") },
                    onValueChange = { label = it }
                )
                OutlinedTextField(
                    value = username,
                    label = { Text("Username") },
                    onValueChange = { username = it }
                )
                OutlinedTextField(
                    value = password,
                    label = { Text("Password") },
                    onValueChange = { password = it }
                )
                OutlinedTextField(
                    value = sendTo,
                    label = { Text("Send to") },
                    onValueChange = { sendTo = it }
                )
                OutlinedTextField(
                    value = smtpServer,
                    label = { Text("SMTP Server") },
                    onValueChange = { smtpServer = it }
                )
                OutlinedTextField(
                    value = smtpServerPort,
                    label = { Text("SMTP Server port") },
                    onValueChange = { smtpServerPort = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { findNavController().navigateUp() }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        val application = requireActivity().application
                        PrefManager.writeAccounts(
                            application,
                            PrefManager.readAccounts(application).value!!.toList() + listOf(
                                Account(
                                    label,
                                    sendTo,
                                    SmtpCredential(
                                        smtpServer,
                                        smtpServerPort.toInt(),
                                        username,
                                        password
                                    )
                                )
                            )
                        )
                        findNavController().navigateUp()
                    }) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
