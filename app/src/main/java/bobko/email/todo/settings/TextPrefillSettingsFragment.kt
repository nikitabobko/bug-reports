package bobko.email.todo.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import bobko.email.todo.LastUsedAppFeatureManager
import bobko.email.todo.PrefManager
import bobko.email.todo.StartedFrom
import bobko.email.todo.util.composeView
import bobko.email.todo.util.observeAsState
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
    val append by LastUsedAppFeatureManager.isFeatureEnabled(requireContext())
        .observeAsState()
    SwitchOrCheckBoxItem(
        "Append app name that shared the text or clipboard",
        checked = append,
        onChecked = {
            this.requireContext().writePref {
//                PrefManager.appendAppNameThatSharedTheText.value = !append TODO
            }
        }
    )
}
