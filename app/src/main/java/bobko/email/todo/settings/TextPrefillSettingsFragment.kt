package bobko.email.todo.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import bobko.email.todo.model.StartedFrom
import bobko.email.todo.model.pref.LastUsedAppFeatureManager
import bobko.email.todo.util.composeView
import bobko.email.todo.util.observeAsState
import bobko.email.todo.util.writePref

class TextPrefillSettingsFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

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
        WhenTheAppIsStartedFromSection(listOf(
            StartedFrom.Launcher.let { it to it.prefillPrefKey!! },
            StartedFrom.Tile.let { it to it.prefillPrefKey!! }
        ))

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
