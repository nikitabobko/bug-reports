package bobko.email.todo

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    val todoTextDraft = MutableLiveData(TextFieldValue(""))
    var todoTextDraftIsChangedAtLeastOnce = MutableLiveData(false)
    var isStartedFromTile: Boolean = false
    var finishActivityAfterSend: Boolean = false
}
