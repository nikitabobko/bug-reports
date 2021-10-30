package bobko.todomail.settings.sendreceiveroute

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
import bobko.todomail.model.SendReceiveRoute
import bobko.todomail.model.SmtpCredential
import bobko.todomail.model.pref.PrefManager
import bobko.todomail.settings.SettingsActivity
import bobko.todomail.settings.SettingsScreen
import bobko.todomail.util.CenteredRow
import bobko.todomail.util.composeView
import bobko.todomail.util.mutableLiveDataOf
import bobko.todomail.util.observeAsMutableState

const val DEFAULT_SMTP_PORT = 25

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
        EditSendReceiveRouteSettingsFragmentScreen(mode)
    }
}

@Composable
private fun EditSendReceiveRouteSettingsFragment.EditSendReceiveRouteSettingsFragmentScreen(mode: Mode) {
    SettingsScreen("Edit Send Receive Route Settings") {
        val sendReceiveRoute = viewModel.sendReceiveRoute.observeAsMutableState { // TODO add screen rotation test
            parentActivity().viewModel.sendReceiveRouteToEdit ?: SendReceiveRoute(
                suggestSendReceiveRouteLabel(requireContext()), "",
                SmtpCredential("", DEFAULT_SMTP_PORT, "", "")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        viewModel.schema.forEach {
            it.Composable(sendReceiveRoute, viewModel)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Buttons(viewModel.schema, sendReceiveRoute, mode)
        Spacer(modifier = Modifier.height(16.dp))
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
                if (schema.filterIsInstance<TextFieldItem<*>>().any { item ->
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

class EditSendReceiveRouteSettingsFragmentViewModel(application: Application) : AndroidViewModel(application) {
    val sendReceiveRoute: MutableLiveData<SendReceiveRoute> = MutableLiveData()
    val showErrorIfFieldIsEmpty = mutableLiveDataOf(false)
    val schema: List<Item> = getSchema(
        existingLabels = PrefManager.readSendReceiveRoutes(application).value.mapTo(mutableSetOf()) { it.label }
    )
}

private enum class Mode {
    Edit, Add
}

fun suggestSendReceiveRouteLabel(context: Context): String {
    val existingLabels = PrefManager.readSendReceiveRoutes(context).value.mapTo(mutableSetOf()) { it.label }
    return sequenceOf("Todo", "Work")
        .plus(generateSequence(0) { it + 1 }.map { "Todo$it" })
        .first { it !in existingLabels }
}
