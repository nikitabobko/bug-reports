package bobko.email.todo

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import bobko.email.todo.util.mutableInitializedLiveDataOf
import bobko.email.todo.util.readPref

class MainActivityViewModel : ViewModel() {
    val todoTextDraft =
        mutableInitializedLiveDataOf(TextFieldValue())
    var todoTextDraftIsChangedAtLeastOnce =
        mutableInitializedLiveDataOf(false)

    private var _startedFrom: StartedFrom? = null
    var startedFrom: StartedFrom
        get() {
            return _startedFrom ?: StartedFrom.Launcher.also { _startedFrom = it }
        }
        set(value) {
            if (_startedFrom == null) {
                _startedFrom = value
            }
        }

    private var isPrefilledWithSharedText: Boolean = false

    fun prefillSharedText(context: Context, sharedText: String, callerAppLabel: String?) {
        require(sharedText.isNotBlank())
        if (todoTextDraft.value.text.isEmpty() &&
            !isPrefilledWithSharedText /* Allow to prefill TextField only once */) {
            isPrefilledWithSharedText = true
            todoTextDraft.value = composeSharedText(context, sharedText, callerAppLabel)
        }
    }

    private fun composeSharedText(
        context: Context,
        sharedText: String,
        callerAppLabel: String?
    ): TextFieldValue {
        val text = buildString {
            appendLine()
            appendLine()
            if (callerAppLabel != null && LastUsedAppFeatureManager.isFeatureEnabled(context).value) {
                appendLine("Shared from: $callerAppLabel")
            }
            append(sharedText)
        }
        return TextFieldValue(text)
    }
}

