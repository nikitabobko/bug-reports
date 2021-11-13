package bobko.todomail.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Email
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import bobko.todomail.R
import bobko.todomail.model.StartedFrom
import bobko.todomail.model.EmailTemplate
import bobko.todomail.model.pref.PrefManager
import bobko.todomail.util.*

class MainSettingsFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PrefManager.readEmailTemplates(requireContext()).value.count() == 0) {
            findNavController().navigate(
                R.id.action_mainSettingsFragment_to_editEmailTemplateSettingsFragment
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = requireContext().composeView {
        MainSettingsActivityScreen(PrefManager.readEmailTemplates(requireContext()))
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainSettingsFragment.MainSettingsActivityScreen(accounts: InitializedLiveData<List<EmailTemplate>>) {
    SettingsScreen("Todomail Settings", rootSettingsScreen = true) {
        TextDivider("Templates")
        TemplatesSection(accounts)

        Divider()
        TextDivider("Close the dialog after send when the app is") // TODO remove this setting
        WhenTheAppIsStartedFromSection(
            listOf(StartedFrom.Launcher, StartedFrom.Tile, StartedFrom.Sharesheet)
                .map { it to it.closeAfterSendPrefKey }
        )

        Divider()
        ListItem(
            modifier = Modifier.clickable {
                findNavController().navigate(
                    R.id.action_mainSettingsFragment_to_textPrefillSettingsFragment
                )
            }
        ) {
            Text("Text prefill settings")
        }

        OutlinedButton(onClick = { /*TODO*/ }, modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text("Reset settings to default")
        }
    }
}

/**
 * Test - [bobko.todomail.settings.CalculateIndexOffsetTest]
 */
fun calculateIndexOffset(pixelOffset: Int, itemHeight: Int) =
    (pixelOffset / (itemHeight / 2)).let { it / 2 + it % 2 }

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MainSettingsFragment.TemplatesSection(
    accountsLiveData: InitializedLiveData<List<EmailTemplate>>
) {
    val accounts by accountsLiveData.observeAsState()
    var offsets by remember(accounts.size) { mutableStateOf(List(accounts.size) { 0 }) }
    var itemHeight by remember { mutableStateOf(0) }
    accounts.forEachIndexed { currentIdx, emailTemplate ->
        val offsetLowerBound = -currentIdx * itemHeight
        val offsetUpperBound = (accounts.lastIndex - currentIdx) * itemHeight
        ListItem(
            icon = {
                val knownCredential = KnownSmtpCredential.values().singleOrNull {
                    emailTemplate.sendTo.endsWith(it.domain)
                }
                if (knownCredential != null) {
                    knownCredential.Icon()
                } else {
                    Icon(
                        Icons.Rounded.Email,
                        "Email (SMTP)",
                        modifier = Modifier.size(emailIconSize)
                    )
                }
            },
            modifier = Modifier
                .offset(y = with(LocalDensity.current) { offsets[currentIdx].toDp() })
                .clickable {
                    findNavController().navigate(
                        R.id.action_mainSettingsFragment_to_editEmailTemplateSettingsFragment
                    )
                }
                .onSizeChanged { if (currentIdx == 0 && itemHeight == 0) itemHeight = it.height },
            trailing = trailing@{
                if (accounts.size <= 1) {
                    return@trailing
                }
                Icon(
                    painterResource(R.drawable.drag_handle_24),
                    "",
                    // TODO Change to swipeable? https://developer.android.com/jetpack/compose/gestures#swiping
                    modifier = Modifier.draggable(
                        rememberDraggableState(onDelta = { delta ->
                            val newOffset = (offsets[currentIdx] + delta.toInt())
                                .coerceIn(offsetLowerBound..offsetUpperBound)
                            val newIdx = currentIdx + calculateIndexOffset(newOffset, itemHeight)
                            offsets = List(offsets.size) {
                                when (it) {
                                    currentIdx -> newOffset

                                    in minOf(newIdx, currentIdx)..maxOf(newIdx, currentIdx) -> {
                                        sign(currentIdx - it) * itemHeight
                                    }

                                    else -> 0
                                }
                            }
                        }),
                        orientation = Orientation.Vertical,
                        onDragStopped = {
                            val newIdx = currentIdx + calculateIndexOffset(
                                offsets[currentIdx],
                                itemHeight
                            )

                            val newAccounts = accounts.toMutableList().apply {
                                removeAt(currentIdx)
                                add(newIdx, emailTemplate)
                            }

                            offsets = List(accounts.size) { 0 }
                            PrefManager.writeEmailTemplates(requireContext(), newAccounts)
                        }
                    )
                )
            },
            text = {
                Row {
                    Text(text = emailTemplate.label)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "From: " + emailTemplate.credential.username)
                        Text(text = "To: " + emailTemplate.sendTo)
                    }
                }
            }
        )
    }
    ListItem(
        icon = {
            Icon(
                Icons.Rounded.Add,
                "",
                modifier = Modifier.size(emailIconSize)
            )
        },
        modifier = Modifier.clickable {
            findNavController().navigate(
                R.id.action_mainSettingsFragment_to_editEmailTemplateSettingsFragment
            )
        },
        text = { Text(text = "Add template") }
    )
}
