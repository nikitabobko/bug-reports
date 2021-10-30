package bobko.todomail.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.findNavController
import bobko.todomail.R
import bobko.todomail.model.SendReceiveRoute
import bobko.todomail.model.SmtpCredential
import bobko.todomail.settings.sendreceiveroute.suggestSendReceiveRouteLabel
import bobko.todomail.theme.EmailTodoTheme
import bobko.todomail.util.CenteredRow
import bobko.todomail.util.composeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddAccountSettingsWizardFragment : BottomSheetDialogFragment() {
    fun parentActivity() = requireActivity() as SettingsActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = requireContext().composeView {
        AddAccountSettingsWizardFragmentScreen()
    }
}

@Composable
fun AddAccountSettingsWizardFragment.AddAccountSettingsWizardFragmentScreen() {
    EmailTodoTheme {
        Surface {
            Column {
                KnownSmtpCredential.values().forEach {
                    MailItem(
                        icon = { it.Icon() },
                        text = it.label,
                        smtpCredentialTemplate = it.smtpCredential
                    )
                }
                MailItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Email,
                            "Email (SMTP)",
                            modifier = Modifier.size(emailIconSize)
                        )
                    },
                    "Email (SMTP)",
                    smtpCredentialTemplate = null
                )
                CenteredRow(modifier = Modifier.padding(8.dp)) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { findNavController().navigateUp() }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AddAccountSettingsWizardFragment.MailItem(
    icon: @Composable () -> Unit,
    text: String,
    smtpCredentialTemplate: SmtpCredential?
) {
    ListItem(
        modifier = Modifier.clickable {
            parentActivity().viewModel.sendReceiveRouteToEdit = smtpCredentialTemplate?.let {
                SendReceiveRoute(suggestSendReceiveRouteLabel(requireContext()), "", it)
            }
            findNavController().navigate(
                R.id.action_addAccountSettingsWizardFragment_to_addAccountSettingsFragmentDialog
            )
        },
        icon = { icon() }
    ) {
        Text(text)
    }
}
