package bobko.todomail.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import bobko.todomail.model.StartedFrom
import bobko.todomail.model.pref.LastUsedAppFeatureManager
import bobko.todomail.util.composeView
import bobko.todomail.util.observeAsState
import bobko.todomail.util.writePref

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
