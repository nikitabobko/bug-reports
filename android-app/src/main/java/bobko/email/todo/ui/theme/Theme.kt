package bobko.email.todo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun EmailTodoTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val systemUiController = rememberSystemUiController()
    MaterialTheme(
        colors = run {
            if (darkTheme) {
                darkColors(
                    primary = Color(0xff1ca15f),
                    secondaryVariant = Color(0xff1ca15f), // Switch checked color
                    secondary = Color(0xff1ca15f) // Checkbox color
                )
            } else {
                lightColors(
                    primary = Color(0xff1ca15f),
                    secondaryVariant = Color(0xff1ca15f), // Switch checked color
                    secondary = Color(0xff1ca15f) // Checkbox color
                )
            }
        },
        content = {
            val color = MaterialTheme.colors.primarySurface
            SideEffect {
                systemUiController.setSystemBarsColor(color = color)
            }
            content()
        }
    )
}