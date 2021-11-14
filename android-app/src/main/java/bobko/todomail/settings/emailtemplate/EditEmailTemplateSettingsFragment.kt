package bobko.todomail.settings.emailtemplate

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
//import bobko.todomail.login.GmailQuickstart
import bobko.todomail.model.EmailTemplate
import bobko.todomail.model.SmtpCredential
import bobko.todomail.model.pref.PrefManager
import bobko.todomail.settings.SettingsActivity
import bobko.todomail.settings.SettingsScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import bobko.todomail.login.sendItSuka
import bobko.todomail.util.*

import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.RuntimeExecutionException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import com.google.api.services.gmail.GmailScopes
//import com.google.api.services.gmail.GmailScopes
//import com.google.api.services.gmail.GmailScopes
import java.lang.IllegalStateException
import java.util.concurrent.ThreadLocalRandom


const val DEFAULT_SMTP_PORT = 25

class EditEmailTemplateSettingsFragment : Fragment() {
    val viewModel by viewModels<EditEmailTemplateSettingsFragmentViewModel>()
    fun parentActivity() = requireActivity() as SettingsActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = requireContext().composeView {
        val existingRoutes = PrefManager.readEmailTemplates(requireContext()).value
        val mode =
            if (parentActivity().viewModel.emailTemplateToEdit in existingRoutes) Mode.Edit
            else Mode.Add
        EditEmailTemplateSettingsFragmentScreen(mode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .setLogSessionId(ThreadLocalRandom.current().nextInt().toString())
            .requestServerAuthCode("473994673878-hpbjfm51euanc0molpthbesm82u3eatl.apps.googleusercontent.com")
            .requestScopes(Scope("https://www.googleapis.com/auth/gmail.send"))
            .requestEmail()
            .build()

        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                GoogleSignIn.getSignedInAccountFromIntent(it.data)
                    .addOnCompleteListener {
                        val account = it.result
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                sendItSuka(account)
                            }
                        }
                    }
                    .addOnFailureListener {
                        errorException(it)
                    }
            } catch (ex: RuntimeExecutionException) {
                errorException(ex)
            }
        }.launch(GoogleSignIn.getClient(requireActivity(), gso).signInIntent)
    }
}

@Composable
private fun EditEmailTemplateSettingsFragment.EditEmailTemplateSettingsFragmentScreen(mode: Mode) {
    SettingsScreen("Edit Email Template Settings") {
        val emailTemplate =
            viewModel.emailTemplate.observeAsMutableState { // TODO add screen rotation test
                parentActivity().viewModel.emailTemplateToEdit ?: EmailTemplate(
                    suggestEmailTemplateLabel(requireContext()), "",
                    SmtpCredential("", DEFAULT_SMTP_PORT, "", "")
                )
            }

        Spacer(modifier = Modifier.height(16.dp))
        viewModel.schema.forEach {
            it.Composable(emailTemplate, viewModel)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Buttons(viewModel.schema, emailTemplate, mode)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun EditEmailTemplateSettingsFragment.Buttons(
    schema: List<Item>,
    emailTemplate: MutableState<EmailTemplate>,
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
                if (schema.filterIsInstance<TextFieldItem<*>>().any { item ->
                        item.getCurrentText(emailTemplate.value)
                            .let { it.isBlank() || item.errorProvider(it) != null }
                    }
                ) {
                    viewModel.showErrorIfFieldIsEmpty.value = true
                } else {
                    PrefManager.writeEmailTemplates(
                        requireContext(),
                        PrefManager.readEmailTemplates(requireContext()).value + listOf(
                            emailTemplate.value
                        )
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

class EditEmailTemplateSettingsFragmentViewModel(application: Application) :
    AndroidViewModel(application) {
    val emailTemplate: MutableLiveData<EmailTemplate> = MutableLiveData()
    val showErrorIfFieldIsEmpty = mutableLiveDataOf(false)
    val schema: List<Item> = getSchema(
        existingLabels = PrefManager.readEmailTemplates(application).value.mapTo(mutableSetOf()) { it.label }
    )
}

private enum class Mode {
    Edit, Add
}

fun suggestEmailTemplateLabel(context: Context): String {
    val existingLabels =
        PrefManager.readEmailTemplates(context).value.mapTo(mutableSetOf()) { it.label }
    return sequenceOf("Todo", "Work")
        .plus(generateSequence(0) { it + 1 }.map { "Todo$it" })
        .first { it !in existingLabels }
}
