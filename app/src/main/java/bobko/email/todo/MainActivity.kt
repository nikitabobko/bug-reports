package bobko.email.todo

import android.app.AppOpsManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import bobko.email.todo.ui.theme.EmailTodoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Some magic to show keyboard on Activity start
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        viewModel.isStartedFromTile = intent.getBooleanExtra(IS_STARTED_FROM_TILE_INTENT_KEY, false)

        val sharedString = intent.takeIf { it?.action == Intent.ACTION_SEND }
            ?.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedString != null) {
            val callerAppLabel = referrer?.host?.let { getAppLabelByPackageName(it) }
                ?: getLastUsedAppLabel()
            viewModel.todoTextDraft.value = composeSharedText(sharedString, callerAppLabel)
            viewModel.finishActivityAfterSend = true
        }

        setContent {
            MainActivityScreen(viewModel)
        }
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
        if (hasFocus && viewModel.isStartedFromTile) {
            viewModel.finishActivityAfterSend = true
            val clipboardManager = getSystemService<ClipboardManager>()
            val clipboard = clipboardManager!!.primaryClip?.getItemAt(0)?.text?.toString()
            if (clipboard != null) {
                viewModel.todoTextDraft.value = composeSharedText(clipboard, getLastUsedAppLabel())
            }
        }
    }

    private fun composeSharedText(sharedText: String, callerAppLabel: String?): TextFieldValue {
        val text = buildString {
            if (callerAppLabel != null) {
                appendLine("From: $callerAppLabel")
                appendLine()
            }
            appendLine(sharedText)
            appendLine()
        }
        return TextFieldValue(text, TextRange(text.length))
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
        private const val IS_STARTED_FROM_TILE_INTENT_KEY = "IS_STARTED_FROM_TILE_INTENT_KEY"

        fun getIntent(context: Context, isStartedFromTile: Boolean): Intent {
            return Intent(context, MainActivity::class.java).apply {
                putExtra(IS_STARTED_FROM_TILE_INTENT_KEY, isStartedFromTile)
            }
        }
    }
}

@Composable
fun MainActivity.MainActivityScreen(viewModel: MainActivityViewModel) {
    EmailTodoTheme {
        var sendInProgress by remember { mutableStateOf(false) }
        var todoTextDraft by viewModel.todoTextDraft.observeAsMutableState()
        val scope = rememberCoroutineScope()
        Column(modifier = Modifier.padding(8.dp)) {
            val focusRequester = remember { FocusRequester() }
            TextField(
                value = todoTextDraft,
                onValueChange = {
                    todoTextDraft = it
                    viewModel.todoTextDraftIsChangedAtLeastOnce.value = true
                },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                enabled = !sendInProgress,
                label = { Text(if (sendInProgress) "Sending..." else "Your todo is...") }
            )
            DisposableEffect(Unit) {
                focusRequester.requestFocus()
                onDispose { }
            }
            Row {
                val onClick = { isWork: Boolean ->
                    scope.launch {
                        val body = todoTextDraft.text.trim()
                        sendInProgress = true
                        todoTextDraft = TextFieldValue("")
                        withContext(Dispatchers.IO) {
                            EmailManager.sendEmailToMyself("|", body, isWork)
                        }
                        sendInProgress = false
                        showToast("Successful!")
                        focusRequester.requestFocus()
                        if (viewModel.finishActivityAfterSend) {
                            this@MainActivityScreen.finish()
                        }
                    }
                    Unit
                }
                TextButton(
                    onClick = { todoTextDraft = TextFieldValue("") },
                    enabled = !sendInProgress && todoTextDraft.text.isNotBlank()
                ) { Text(text = "Clear") }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = { onClick(true) },
                    enabled = !sendInProgress && todoTextDraft.text.isNotBlank()
                ) { Text("Work") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = { onClick(false) },
                    enabled = !sendInProgress && todoTextDraft.text.isNotBlank()
                ) { Text("Send") }
            }
        }
    }
}
