package bobko.email.todo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun EmailTodoTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colors = run {
            if (darkTheme) darkColors(primary = Color(0xff1ca15f))
            else lightColors(primary = Color(0xff1ca15f))
        },
        content = {
            Surface {
                content()
            }
        }
    )
}