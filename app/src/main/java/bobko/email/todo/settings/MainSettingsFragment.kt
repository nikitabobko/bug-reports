package bobko.email.todo.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import bobko.email.todo.PrefManager
import bobko.email.todo.R
import bobko.email.todo.model.Account
import bobko.email.todo.ui.theme.EmailTodoTheme
import bobko.email.todo.util.NotNullableLiveData
import bobko.email.todo.util.SizedSequence
import bobko.email.todo.util.composeView
import bobko.email.todo.util.observeAsNotNullableState

class MainSettingsFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PrefManager.readAccounts(requireActivity().application).value.count() == 0) {
            findNavController().navigate(R.id.action_mainSettingsFragment_to_addAccountSettingsWizardFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = requireContext().composeView {
        MainSettingsActivityScreen(PrefManager.readAccounts(requireActivity().application))
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainSettingsFragment.MainSettingsActivityScreen(accounts: NotNullableLiveData<SizedSequence<Account>>) {
    EmailTodoTheme {
        var accountToDeleteConfirmation by remember { mutableStateOf<Account?>(null) }
        Surface {
            Column {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = { Text("Email TODO Settings") },
                    navigationIcon = {
                        IconButton(onClick = { requireActivity().finish() }) {
                            Icon(Icons.Rounded.ArrowBack, "")
                        }
                    }
                )

                DividerWithText("Accounts")
                val accountsSeq by accounts.observeAsNotNullableState()
                accountsSeq.forEach { account ->
                    ListItem(
                        icon = {
                            knownSmtpCredentials
                                .singleOrNull {
                                    it.smtpCredential.smtpServer == account.credential.smtpServer
                                }
                                ?.Icon()
                        },
                        modifier = Modifier.clickable {
                            findNavController().navigate(
                                R.id.action_mainSettingsFragment_to_addAccountSettingsWizardFragment
                            )
                        },
                        trailing = {
                            IconButton(
                                content = { Icon(Icons.Rounded.Delete, "") },
                                onClick = { accountToDeleteConfirmation = account }
                            )
                        },
                        text = { Text(text = "${account.label} <${account.sendTo}>") }
                    )
                }
                ListItem(
                    icon = { Icon(Icons.Rounded.Add, "", modifier = Modifier.size(emailIconSize)) },
                    modifier = Modifier.clickable {
                        findNavController().navigate(
                            R.id.action_mainSettingsFragment_to_addAccountSettingsWizardFragment
                        )
                    },
                    text = { Text(text = "Add account") }
                )

                DividerWithText("Prefill with clipboard when ...")
                SwitchItem("... opened from Launcher")
                SwitchItem("... opened from Tile")

                DividerWithText("Other")
                SwitchItem("Append app name which shared prefilled text")
            }
        }
        accountToDeleteConfirmation?.let { account ->
            AlertDialog(
                onDismissRequest = { accountToDeleteConfirmation = null },
                text = { Text("Are you sure you want to delete account \"${account.label} <${account.sendTo}>\"?") },
                buttons = {
                    Row(modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)) {
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            accountToDeleteConfirmation = null
                        }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            val application = requireActivity().application
                            PrefManager.writeAccounts(
                                application,
                                accounts.value.toList().filter { it != account }
                            )
                            accountToDeleteConfirmation = null
                        }) { Text("Yes") }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwitchItem(text: String) {
    var checked by remember { mutableStateOf(true) }
    ListItem(
        icon = { Spacer(modifier = Modifier.width(32.dp)) },
        trailing = {
            Switch(checked = checked, onCheckedChange = null)
        },
        modifier = Modifier.clickable { checked = !checked }
    ) {
        Text(text)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DividerWithText(text: String) {
    ListItem(modifier = Modifier.height(32.dp)) {
        Text(text, color = MaterialTheme.colors.primary, fontWeight = FontWeight.Bold)
    }
}
