package bobko.email.todo.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import bobko.email.todo.PrefManager
import bobko.email.todo.StartedFrom
import bobko.email.todo.ui.theme.EmailTodoTheme
import bobko.email.todo.util.composeView
import bobko.email.todo.util.observeAsNotNullableState
import bobko.email.todo.util.readPref
import bobko.email.todo.util.writePref

class TextPrefillSettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = requireContext().composeView {
        TextPrefillSettingsScreen()
    }
}

@Composable
private fun TextPrefillSettingsFragment.TextPrefillSettingsScreen() {
    SettingsScreen(title = "Text prefill settings") {
        TextDivider("Prefill with clipboard when the app is")
        val prefillWithClipboard = listOf(
            StartedFrom.Launcher.let { it to it.prefillPrefKey!! },
            StartedFrom.Tile.let { it to it.prefillPrefKey!! }
        )
        WhenTheAppIsStartedFromSection(prefillWithClipboard)

        Divider()
        OtherSettingsSection()
    }
}

@Composable
private fun TextPrefillSettingsFragment.OtherSettingsSection() {
    val append by requireContext()
        .readPref { PrefManager.appendAppNameThatSharedTheText.liveData }
        .observeAsNotNullableState()
    SwitchOrCheckBoxItem(
        "Append app name that shared the text or clipboard",
        checked = append,
        onChecked = {
            this.requireContext().writePref {
                PrefManager.appendAppNameThatSharedTheText.value = !append
            }
        }
    )
}
