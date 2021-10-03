package bobko.email.todo

import android.app.AppOpsManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.getSystemService
import bobko.email.todo.model.Account
import bobko.email.todo.settings.SettingsActivity
import bobko.email.todo.ui.theme.EmailTodoTheme
import bobko.email.todo.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    val viewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Some magic to show keyboard on Activity start
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        (intent.getSerializableExtra(STARTED_FROM) as? StartedFrom)?.let {
            viewModel.startedFrom = it
        }

        val sharedText = intent.takeIf { it?.action == Intent.ACTION_SEND }
            ?.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText?.isNotBlank() == true) {
            viewModel.startedFrom = StartedFrom.Sharesheet
            val callerAppLabel = referrer?.host?.let { getAppLabelByPackageName(it) }
                ?: LastUsedAppProvider.getLastUsedAppLabel(this)
            viewModel.prefillSharedText(this, sharedText, callerAppLabel)
        }

        val accounts = PrefManager.readAccounts(this@MainActivity)
        if (accounts.value.count() == 0) {
            finish()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        setContent {
            MainActivityScreen(accounts)
        }
        window.setGravity(Gravity.BOTTOM)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onStart() {
        super.onStart()
        // Look for usages of android.app.usage.UsageStatsManager in the app
        if (!isUsageAccessGranted()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Since Android Q it's necessary for the app to have focus to be able to access clipboard.
        if (!hasFocus) {
            return
        }
        val shouldPrefillWithClipboard = when (val startedFrom = viewModel.startedFrom) {
            StartedFrom.Launcher, StartedFrom.Tile -> readPref {
                startedFrom.prefillPrefKey!!.value
            }
            StartedFrom.Sharesheet -> false
        }
        if (!shouldPrefillWithClipboard) {
            return
        }
        val clipboardManager = getSystemService<ClipboardManager>()
        val clipboard = clipboardManager!!.primaryClip?.getItemAt(0)?.text?.toString()
        if (clipboard?.isNotBlank() != true) {
            return
        }
        viewModel.prefillSharedText(this, clipboard, LastUsedAppProvider.getLastUsedAppLabel(this))
    }

    private fun isUsageAccessGranted(): Boolean {
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val appOpsManager = getSystemService<AppOpsManager>()!!
        return AppOpsManager.MODE_ALLOWED == appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            applicationInfo.uid, applicationInfo.packageName
        )
    }

    companion object {
        private const val STARTED_FROM = "STARTED_FROM"

        fun getIntent(context: Context, isStartedFromTile: Boolean): Intent {
            return Intent(context, MainActivity::class.java).apply {
                if (isStartedFromTile) {
                    putExtra(STARTED_FROM, StartedFrom.Tile)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainActivity.MainActivityScreen(accountsLive: NotNullableLiveData<List<Account>>) {
    EmailTodoTheme {
        Column {
            // Transparent Surface for keeping space for Android context menu
            Surface(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .clickable(
                        remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { finish() }
                    ),
                color = Color.Transparent
            ) {}
            Surface(
                modifier = Modifier.clip(
                    RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val sendInProgress = remember { mutableStateOf(false) }
                    val todoTextDraft = viewModel.todoTextDraft.observeAsNotNullableMutableState()
                    val focusRequester = remember { FocusRequester() }
                    val isError = remember { mutableStateOf(false) }
                    TextField(
                        value = todoTextDraft.value,
                        isError = isError.value,
                        onValueChange = {
                            todoTextDraft.value = it
                            viewModel.todoTextDraftIsChangedAtLeastOnce.value = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        ),
                        enabled = !sendInProgress.value,
                        label = {
                            Text(
                                when {
                                    isError.value -> "Error" // TODO better message
                                    sendInProgress.value -> "Sending..."
                                    else -> "Your todo is..."
                                }
                            )
                        }
                    )
                    Buttons(todoTextDraft, isError, sendInProgress, accountsLive)
                    DisposableEffect(sendInProgress.value, todoTextDraft.value) {
                        focusRequester.requestFocus()
                        onDispose { }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainActivity.Buttons(
    todoTextDraft: MutableState<TextFieldValue>,
    isError: MutableState<Boolean>,
    sendInProgress: MutableState<Boolean>,
    accountsLive: NotNullableLiveData<List<Account>>
) {
    val canStartSending = !sendInProgress.value && todoTextDraft.value.text.isNotBlank()
    val unspecifiedOrErrorColor =
        if (isError.value && canStartSending) MaterialTheme.colors.error else Color.Unspecified
    val greenOrErrorColor =
        if (isError.value) MaterialTheme.colors.error else MaterialTheme.colors.primary
    val scope = rememberCoroutineScope()
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(
            onClick = {
                todoTextDraft.value = TextFieldValue()
                isError.value = false
            },
            enabled = canStartSending
        ) { Text(text = "Clear", color = unspecifiedOrErrorColor) }
        IconButton(onClick = {
            startActivity(Intent(this@Buttons, SettingsActivity::class.java))
        }) { Icon(Icons.Rounded.Settings, "", tint = greenOrErrorColor) }

        Spacer(modifier = Modifier.weight(1f))

        val onClick = { account: Account ->
            scope.launch {
                val prevText = todoTextDraft.value.text
                sendInProgress.value = true
                todoTextDraft.value = TextFieldValue()
                try {
                    withContext(Dispatchers.IO) {
                        EmailManager.sendEmailToMyself(account, "|", prevText.trim())
                    }
                    isError.value = false
                    showToast("Successful!")
                    val shouldCloseAfterSend =
                        readPref { viewModel.startedFrom.closeAfterSendPrefKey.value }
                    if (shouldCloseAfterSend) {
                        this@Buttons.finish()
                    }
                } catch (ex: Throwable) {
                    todoTextDraft.value = TextFieldValue(prevText)
                    isError.value = true
                } finally {
                    sendInProgress.value = false
                }
            }
            Unit
        }
        val minButtonsToStartFolding = 4
        val numOfButtonsToFoldDownTo = 2
        val accounts by accountsLive.observeAsNotNullableState()
        val doFold = accounts.count() >= minButtonsToStartFolding
        if (doFold) {
            Box {
                var expanded by remember { mutableStateOf(false) }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    properties = PopupProperties(focusable = false) // Don't hide keyboard
                ) {
                    accounts.drop(numOfButtonsToFoldDownTo)
                        .forEach {
                            DropdownMenuItem(onClick = { onClick(it) }) {
                                Text(it.label, color = greenOrErrorColor)
                            }
                        }
                }
                IconButton(onClick = { expanded = true }, enabled = canStartSending) {
                    if (canStartSending) {
                        Icon(Icons.Rounded.MoreVert, "", tint = greenOrErrorColor)
                    } else {
                        Icon(Icons.Rounded.MoreVert, "")
                    }
                }
            }
        }

        accounts.let { if (doFold) it.take(numOfButtonsToFoldDownTo) else it }
            .reversed()
            .forEach {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { onClick(it) }, enabled = canStartSending) {
                    Text(it.label, color = unspecifiedOrErrorColor)
                }
            }
    }
}
