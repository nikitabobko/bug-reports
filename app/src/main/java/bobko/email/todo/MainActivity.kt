package bobko.email.todo

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
        val scaffoldState = rememberScaffoldState()
        var sendInProgress by remember { mutableStateOf(false) }
        var textFieldValue by remember { mutableStateOf("") }
        textFieldValue = textFieldValue.dropWhile { it.isWhitespace() }
        val scope = rememberCoroutineScope()
        Scaffold(
            scaffoldState = scaffoldState,
            floatingActionButton = {
                Row {
                    val onClick = { isWork: Boolean ->
                        scope.launch {
                            val text = textFieldValue
                            sendInProgress = true
                            textFieldValue = "Sending..."
                            withContext(Dispatchers.IO) {
                                EmailManager.sendEmailToMyself(text, isWork)
                            }
                            textFieldValue = ""
                            sendInProgress = false
                            scaffoldState.snackbarHostState.showSnackbar("Successful!")
                        }
                        Unit
                    }
                    Button(
                        onClick = { onClick(true) },
                        enabled = !sendInProgress && textFieldValue.isNotBlank()
                    ) { Text("Work") }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { onClick(false) },
                        enabled = !sendInProgress && textFieldValue.isNotBlank()
                    ) { Text("Send") }
                }
            },
            topBar = {
                TopAppBar(title = { Text("Email TODO") })
            },
        ) {
            val focusRequester = remember { FocusRequester() }
            TextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                enabled = !sendInProgress
            )
            DisposableEffect(Unit) {
                focusRequester.requestFocus()
                onDispose { }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainActivityScreen()
}