package bobko.email.todo

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import bobko.email.todo.ui.theme.EmailTodoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Some magic to keep FAB above IME keyboard
        // https://stackoverflow.com/questions/64050392/software-keyboard-overlaps-content-of-jetpack-compose-view
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setContent {
            MainActivityScreen()
        }
    }
}

@Composable
fun MainActivityScreen() {
    EmailTodoTheme {
        var sendInProgress by remember { mutableStateOf(false) }
        var textFieldValue by remember { mutableStateOf(TextFieldValue()) }
        val scope = rememberCoroutineScope()
        Scaffold(
            floatingActionButton = {
                if (!sendInProgress) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                val subject = textFieldValue.text.lines().first().trim()
                                val text = textFieldValue.text.lines().drop(1).joinToString("\n").trim()
                                sendInProgress = true
                                textFieldValue = TextFieldValue("In progress...")
                                withContext(Dispatchers.IO) {
                                    GmailManager.sendEmailToMyself(subject, text)
                                }
                                textFieldValue = TextFieldValue()
                                sendInProgress = false
                            }
                        },
                    ) { Text("Send") }
                }
            },
            topBar = {
                TopAppBar(title = { Text("Email TODO") })
            },
        ) {
            Box {
                TextField(
                    value = textFieldValue, onValueChange = { textFieldValue = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    enabled = !sendInProgress
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainActivityScreen()
}