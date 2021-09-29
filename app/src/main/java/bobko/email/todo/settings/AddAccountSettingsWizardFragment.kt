package bobko.email.todo.settings

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import bobko.email.todo.R
import bobko.email.todo.model.Account
import bobko.email.todo.model.SmtpCredential
import bobko.email.todo.ui.theme.EmailTodoTheme
import bobko.email.todo.util.composeView
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
                knownSmtpCredentials.forEach {
                    MailItem(
                        icon = { it.Icon() },
                        text = it.label,
                        smtpTemplate = it.smtpCredential
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
                    smtpTemplate = null
                )
                Row(modifier = Modifier.padding(8.dp)) {
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
    smtpTemplate: SmtpCredential?
) {
    ListItem(
        modifier = Modifier.clickable {
            parentActivity().viewModel.accountTemplate = MutableLiveData(
                smtpTemplate?.let { Account("Todo", "to@gmail.com", it) }
            )
            findNavController().navigate(R.id.action_addAccountSettingsWizardFragment_to_addAccountSettingsFragmentDialog)
        },
        icon = { icon() }
    ) {
        Text(text)
    }
}
