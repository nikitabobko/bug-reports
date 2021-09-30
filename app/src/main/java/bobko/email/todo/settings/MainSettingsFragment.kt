package bobko.email.todo.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import bobko.email.todo.PrefManager
import bobko.email.todo.R
import bobko.email.todo.model.Account
import bobko.email.todo.ui.theme.EmailTodoTheme
import bobko.email.todo.util.*

class MainSettingsFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PrefManager.readAccounts(requireActivity().application).value.count() == 0) {
            findNavController().navigate(R.id.action_mainSettingsFragment_to_addAccountSettingsWizardFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = requireContext().composeView {
        MainSettingsActivityScreen(PrefManager.readAccounts(requireActivity().application))
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainSettingsFragment.MainSettingsActivityScreen(accounts: NotNullableLiveData<SizedSequence<Account>>) {
    EmailTodoTheme {
        Surface {
            val scrollState = rememberScrollState()
            Column {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = { Text("Email TODO Settings") },
                    navigationIcon = {
                        IconButton(onClick = { requireActivity().finish() }) {
                            Icon(Icons.Rounded.ArrowBack, "")
                        }
                    }
                )

                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    DividerWithText("Accounts")
                    Accounts(accounts)
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

                    DividerWithText("Prefill with clipboard when ...")
                    SwitchItem("... opened from Launcher")
                    SwitchItem("... opened from Tile")

                    DividerWithText("Other")
                    SwitchItem("Append app name which shared prefilled text")
                }
            }
        }
    }
}

private fun calculateIndexOffset(pixelOffset: Int, itemHeight: Int) =
    (pixelOffset / (itemHeight / 2)).let { it / 2 + it % 2 }

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MainSettingsFragment.Accounts(accountsLiveData: NotNullableLiveData<SizedSequence<Account>>) {
    val accounts = accountsLiveData.observeAsNotNullableState().value.toList()
    var offsets by remember(accounts.size) { mutableStateOf(List(accounts.size) { 0 }) }
    var itemHeight by remember { mutableStateOf(0) }
    accounts.forEachIndexed { currentIdx, account ->
        val offsetLowerBound = -currentIdx * itemHeight
        val offsetUpperBound = (accounts.lastIndex - currentIdx) * itemHeight
        ListItem(
            icon = {
                knownSmtpCredentials
                    .singleOrNull {
                        it.smtpCredential.smtpServer == account.credential.smtpServer
                    }
                    ?.Icon()
            },
            modifier = Modifier
                .offset(y = with(LocalDensity.current) { offsets[currentIdx].toDp() })
                .clickable {
                    findNavController().navigate(
                        R.id.action_mainSettingsFragment_to_addAccountSettingsWizardFragment
                    )
                }
                .onSizeChanged { if (currentIdx == 0 && itemHeight == 0) itemHeight = it.height },
            trailing = {
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
                            PrefManager.writeAccounts(requireActivity().application, newAccounts)
                        }
                    )
                )
            },
            text = { Text(text = "${account.label} <${account.sendTo}>") }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwitchItem(text: String) {
    var checked by remember { mutableStateOf(true) }
    ListItem(
        icon = { Spacer(modifier = Modifier.width(32.dp)) },
        trailing = {
            Switch(checked = checked, onCheckedChange = null)
        },
        modifier = Modifier.clickable { checked = !checked }
    ) {
        Text(text)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DividerWithText(text: String) {
    ListItem(modifier = Modifier.height(32.dp)) {
        Text(text, color = MaterialTheme.colors.primary, fontWeight = FontWeight.Bold)
    }
}
