package bobko.email.todo.settings

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
import bobko.email.todo.R
import bobko.email.todo.model.StartedFrom
import bobko.email.todo.model.Account
import bobko.email.todo.model.pref.PrefManager
import bobko.email.todo.util.*

class MainSettingsFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PrefManager.readAccounts(requireContext()).value.count() == 0) {
            findNavController().navigate(
                R.id.action_mainSettingsFragment_to_addAccountSettingsWizardFragment
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = requireContext().composeView {
        MainSettingsActivityScreen(PrefManager.readAccounts(requireContext()))
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainSettingsFragment.MainSettingsActivityScreen(accounts: InitializedLiveData<List<Account>>) {
    SettingsScreen("Email TODO Settings", rootSettingsScreen = true) {
        TextDivider("Accounts")
        AccountsSection(accounts)

        Divider()
        TextDivider("Close the dialog after send when the app is")
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

        OutlinedButton(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Reset settings to default")
        }
    }
}

private fun calculateIndexOffset(pixelOffset: Int, itemHeight: Int) =
    (pixelOffset / (itemHeight / 2)).let { it / 2 + it % 2 }

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MainSettingsFragment.AccountsSection(
    accountsLiveData: InitializedLiveData<List<Account>>
) {
    val accounts by accountsLiveData.observeAsState()
    var offsets by remember(accounts.size) { mutableStateOf(List(accounts.size) { 0 }) }
    var itemHeight by remember { mutableStateOf(0) }
    accounts.forEachIndexed { currentIdx, account ->
        val offsetLowerBound = -currentIdx * itemHeight
        val offsetUpperBound = (accounts.lastIndex - currentIdx) * itemHeight
        ListItem(
            icon = {
                val knownCredential = knownSmtpCredentials.singleOrNull {
                    it.smtpCredential.smtpServer == account.credential.smtpServer
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
                        R.id.action_mainSettingsFragment_to_addAccountSettingsWizardFragment
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

                            val newAccounts = when {
                                currentIdx < newIdx -> accounts.subList(0, currentIdx) +
                                        accounts.subList(currentIdx + 1, newIdx + 1) +
                                        listOf(account) +
                                        accounts.subList(newIdx + 1, accounts.size)
                                newIdx < currentIdx -> accounts.subList(0, newIdx) +
                                        listOf(account) +
                                        accounts.subList(newIdx, currentIdx) +
                                        accounts.subList(currentIdx + 1, accounts.size)
                                else -> accounts
                            }

                            offsets = List(accounts.size) { 0 }
                            PrefManager.writeAccounts(requireContext(), newAccounts)
                        }
                    )
                )
            },
            text = {
                Column {
                    Text(text = account.label)
                    Text(text = account.sendTo)
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
                R.id.action_mainSettingsFragment_to_addAccountSettingsWizardFragment
            )
        },
        text = { Text(text = "Add account") }
    )
}
