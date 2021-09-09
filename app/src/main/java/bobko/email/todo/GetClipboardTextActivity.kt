package bobko.email.todo

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity

// Since Android Q it's necessary for the app to have focus to be able to access clipboard.
// Okay, let's show empty activity, copy to clipboard and close it
class GetClipboardTextActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        println("Hi")
    }
}
