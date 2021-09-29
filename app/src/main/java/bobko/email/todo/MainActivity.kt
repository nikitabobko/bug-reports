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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import bobko.email.todo.model.Account
import bobko.email.todo.settings.SettingsActivity
import bobko.email.todo.ui.theme.EmailTodoTheme
import bobko.email.todo.util.*
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

        val sharedText = intent.takeIf { it?.action == Intent.ACTION_SEND }
            ?.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText?.isNotBlank() == true) {
            val callerAppLabel = referrer?.host?.let { getAppLabelByPackageName(it) }
                ?: getLastUsedAppLabel()
            viewModel.prefillSharedText(sharedText, callerAppLabel)
        }

        val accounts = PrefManager.readAccounts(application)
        if (accounts.value.count() == 0) {
            finish()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        setContent {
            MainActivityScreen(viewModel, accounts)
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
        if (hasFocus && viewModel.isStartedFromTile) {
            val clipboardManager = getSystemService<ClipboardManager>()
            val clipboard = clipboardManager!!.primaryClip?.getItemAt(0)?.text?.toString()
            if (clipboard?.isNotBlank() == true) {
                viewModel.prefillSharedText(clipboard, getLastUsedAppLabel())
            }
        }
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
fun MainActivity.MainActivityScreen(
    viewModel: MainActivityViewModel,
    accounts: NotNullableLiveData<SizedSequence<Account>>
) {
    EmailTodoTheme {
        var sendInProgress by remember { mutableStateOf(false) }
        var todoTextDraft by viewModel.todoTextDraft.observeAsNotNullableMutableState()
        val scope = rememberCoroutineScope()
        val focusRequester = remember { FocusRequester() }
        Surface(modifier = Modifier.clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))) {
            Column(modifier = Modifier.padding(8.dp)) {
                TextField(
                    value = todoTextDraft,
                    onValueChange = {
                        todoTextDraft = it
                        viewModel.todoTextDraftIsChangedAtLeastOnce.value = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    enabled = !sendInProgress,
                    label = { Text(if (sendInProgress) "Sending..." else "Your todo is...") }
                )
                DisposableEffect(sendInProgress) {
                    focusRequester.requestFocus()
                    onDispose { }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { todoTextDraft = TextFieldValue() },
                        enabled = !sendInProgress && todoTextDraft.text.isNotBlank()
                    ) { Text(text = "Clear") }
                    IconButton(onClick = {
                        startActivity(Intent(this@MainActivityScreen, SettingsActivity::class.java))
                    }) {
                        Icon(Icons.Rounded.Settings, "", tint = MaterialTheme.colors.primary)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    val onClick = { account: Account ->
                        scope.launch {
                            val body = todoTextDraft.text.trim()
                            sendInProgress = true
                            todoTextDraft = TextFieldValue()
                            withContext(Dispatchers.IO) {
                                EmailManager.sendEmailToMyself(account, "|", body)
                            }
                            sendInProgress = false
                            showToast("Successful!")
                            if (viewModel.finishActivityAfterSend) {
                                this@MainActivityScreen.finish()
                            }
                        }
                        Unit
                    }
                    val accountsSeq by accounts.observeAsNotNullableState()
                    accountsSeq.forEach {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { onClick(it) },
                            enabled = !sendInProgress && todoTextDraft.text.isNotBlank()
                        ) { Text(it.label) }
                    }
                }
            }
        }
    }
}
