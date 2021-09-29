package bobko.email.todo

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import bobko.email.todo.util.NotNullableMutableLiveData

class MainActivityViewModel : ViewModel() {
    val todoTextDraft = NotNullableMutableLiveData(TextFieldValue())
    var todoTextDraftIsChangedAtLeastOnce = NotNullableMutableLiveData(false)
    var isStartedFromTile: Boolean = false

    private var isPrefilledWithSharedText: Boolean = false
    val finishActivityAfterSend: Boolean get() = isPrefilledWithSharedText

    fun prefillSharedText(sharedText: String, callerAppLabel: String?) {
        require(sharedText.isNotBlank())
        if (todoTextDraft.value.text.isEmpty() && !isPrefilledWithSharedText /* Allow to prefill TextField only once */) {
            isPrefilledWithSharedText = true
            todoTextDraft.value = composeSharedText(sharedText, callerAppLabel)
        }
    }

    private fun composeSharedText(sharedText: String, callerAppLabel: String?): TextFieldValue {
        val text = buildString {
            appendLine()
            appendLine()
            if (callerAppLabel != null) {
                appendLine("From: $callerAppLabel")
            }
            append(sharedText)
        }
        return TextFieldValue(text)
    }
}
