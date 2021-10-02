package bobko.email.todo.settings

import android.os.Build
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import bobko.email.todo.PrefManager
import bobko.email.todo.R
import bobko.email.todo.StartedFrom
import bobko.email.todo.model.Account
import bobko.email.todo.ui.theme.EmailTodoTheme
import bobko.email.todo.util.*

class MainSettingsFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PrefManager.readAccounts(requireContext()).value.count() == 0) {
            findNavController().navigate(R.id.action_mainSettingsFragment_to_addAccountSettingsWizardFragment)
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
fun MainSettingsFragment.MainSettingsActivityScreen(accounts: NotNullableLiveData<List<Account>>) {
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
                    TextDivider("Accounts")
                    AccountsSection(accounts)

                    TextDivider("Close the dialog after send when the app is")
                    WhenTheAppIsStartedFromSection(
                        listOf(StartedFrom.Launcher, StartedFrom.Tile, StartedFrom.Sharesheet)
                            .map { it to it.closeAfterSendPrefKey }
                    )

                    TextDivider("Prefill with clipboard when the app is")
                    val prefillWithClipboard = listOf(
                        StartedFrom.Launcher.let { it to it.prefillPrefKey!! },
                        StartedFrom.Tile.let { it to it.prefillPrefKey!! }
                    )
                    WhenTheAppIsStartedFromSection(prefillWithClipboard)

                    TextDivider("Other settings")
                    OtherSettingsSection(
                        enableAppendAppName = prefillWithClipboard.any { (_, prefKey) ->
                            requireContext().readPref { prefKey.liveData }
                                .observeAsNotNullableState().value
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainSettingsFragment.OtherSettingsSection(
    enableAppendAppName: Boolean
) {
    val append by requireContext()
        .readPref { PrefManager.appendAppNameThatSharedTheText.liveData }
        .observeAsNotNullableState()
    SwitchOrCheckBoxItem(
        "Append app name that shared the text",
        checked = append,
        onChecked = if (enableAppendAppName) {
            {
                requireContext().writePref {
                    PrefManager.appendAppNameThatSharedTheText.value = !append
                }
            }
        } else null
    )
}

@Composable
private fun MainSettingsFragment.WhenTheAppIsStartedFromSection(
    whenStartedFrom: List<Pair<StartedFrom, PrefKey<Boolean>>>
) {
    whenStartedFrom.forEach { (startedFrom, prefKey) ->
        if (startedFrom == StartedFrom.Tile && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            SwitchOrCheckBoxItem(
                startedFrom.text,
                description = "Tiles are available only since Android N",
                checked = false,
                onChecked = null,
                useCheckBox = true
            )
        } else {
            val checked by requireContext().readPref { prefKey.liveData }
                .observeAsNotNullableState()
            SwitchOrCheckBoxItem(
                startedFrom.text,
                checked = checked,
                onChecked = {
                    requireContext().writePref {
                        prefKey.value = !checked
                    }
                },
                useCheckBox = true
            )
        }
    }
}

private fun calculateIndexOffset(pixelOffset: Int, itemHeight: Int) =
    (pixelOffset / (itemHeight / 2)).let { it / 2 + it % 2 }

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MainSettingsFragment.AccountsSection(accountsLiveData: NotNullableLiveData<List<Account>>) {
    val accounts = accountsLiveData.observeAsNotNullableState().value
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
                            PrefManager.writeAccounts(requireContext(), newAccounts)
                        }
                    )
                )
            },
            text = { Text(text = "${account.label} <${account.sendTo}>") }
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwitchOrCheckBoxItem(
    text: String,
    checked: Boolean,
    onChecked: (() -> Unit)?,
    description: String? = null,
    useCheckBox: Boolean = false
) {
    ListItem(
        icon = { Spacer(modifier = Modifier.width(emailIconSize)) },
        trailing = {
            if (useCheckBox) {
                Checkbox(checked = checked, onCheckedChange = null, enabled = onChecked != null)
            } else {
                Switch(checked = checked, onCheckedChange = null, enabled = onChecked != null)
            }
        },
        modifier = run {
            if (onChecked != null) Modifier.clickable(onClick = onChecked)
            else Modifier
        }
    ) {
        val disabledContentColor: Color = MaterialTheme.colors.onSurface
            .copy(alpha = ContentAlpha.disabled)
        val content = @Composable {
            if (description != null) {
                Column {
                    Text(text)
                    Text(
                        description,
                        color = disabledContentColor,
                        style = MaterialTheme.typography.caption
                    )
                }
            } else {
                Text(text)
            }
        }
        if (onChecked == null) {
            CompositionLocalProvider(LocalContentAlpha provides disabledContentColor.alpha) {
                content()
            }
        } else {
            content()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TextDivider(text: String) {
    ListItem(modifier = Modifier.height(32.dp)) {
        Text(text, color = MaterialTheme.colors.primary, style = MaterialTheme.typography.subtitle2)
    }
}
