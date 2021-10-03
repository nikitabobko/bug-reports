package bobko.email.todo.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import bobko.email.todo.model.StartedFrom
import bobko.email.todo.ui.theme.EmailTodoTheme
import bobko.email.todo.util.*

@Composable
fun Fragment.SettingsScreen(
    title: String,
    rootSettingsScreen: Boolean = false,
    content: @Composable () -> Unit
) {
    EmailTodoTheme {
        Surface {
            val scrollState = rememberScrollState()
            Column {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (rootSettingsScreen) {
                                    requireActivity().finish()
                                } else {
                                    findNavController().navigateUp()
                                }
                            }
                        ) {
                            Icon(Icons.Rounded.ArrowBack, "")
                        }
                    }
                )
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TextDivider(text: String) {
    ListItem(modifier = Modifier.height(32.dp)) {
        Text(text, color = MaterialTheme.colors.primary, style = MaterialTheme.typography.subtitle2)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwitchOrCheckBoxItem(
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

@Composable
fun WhenTheAppIsStartedFromSection(
    whenStartedFrom: List<Pair<StartedFrom, PrefKey<Boolean>>>
) {
    whenStartedFrom.forEach { (startedFrom, prefKey) ->
        if (startedFrom == StartedFrom.Tile && !doesSupportTiles) {
            SwitchOrCheckBoxItem(
                startedFrom.text,
                description = "Tiles are available only since Android N",
                checked = false,
                onChecked = null,
                useCheckBox = true
            )
        } else {
            val context = LocalContext.current
            val checked by context.readPref { prefKey.initializedLiveData }.observeAsState()
            SwitchOrCheckBoxItem(
                startedFrom.text,
                checked = checked,
                onChecked = {
                    context.writePref {
                        prefKey.value = !checked
                    }
                },
                useCheckBox = true
            )
        }
    }
}
