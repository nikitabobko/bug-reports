package bobko.email.todo

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        var textFieldValue by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()
        Scaffold(
            floatingActionButton = {
                if (!sendInProgress) {
//                    Row {
//                        Button(onClick = { /*TODO*/ }) {
//                            Text(text = "JB todo")
//                        }
//                        Spacer(modifier = Modifier.width(16.dp))
//                        Button(onClick = { /*TODO*/ }) {
//                            Text(text = "todo")
//                        }
//                    }
                    Column {
                        val context = LocalContext.current
                        FloatingActionButton(
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        context,
                                        SendEmailFromClipboardAutocloseableActivity::class.java
                                    )
                                )
                            },
                        ) { Text("Work") }
                        Spacer(modifier = Modifier.height(16.dp))
                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    val (subject, text) = textFieldValue.lines().withIndex()
                                        .partition { it.index == 0 }
                                        .let { (subjectLines, textLines) ->
                                            listOf(subjectLines, textLines)
                                        }
                                        .map { subjectOrText ->
                                            subjectOrText.joinToString("\n") { it.value }.trim()
                                        }
                                    sendInProgress = true
                                    textFieldValue = "Sending..."
                                    withContext(Dispatchers.IO) {
                                        EmailManager.sendEmailToMyself(subject, text)
                                    }
                                    textFieldValue = ""
                                    sendInProgress = false
                                }
                            },
                        ) { Text("Send") }
                    }
                }
            },
            topBar = {
                TopAppBar(title = { Text("Email TODO") })
            },
        ) {
            TextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainActivityScreen()
}