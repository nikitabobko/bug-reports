package bobko.todomail

import android.app.Application
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import bobko.todomail.model.StartedFrom
import bobko.todomail.model.pref.LastUsedAppFeatureManager
import bobko.todomail.model.pref.PrefManager
import bobko.todomail.util.mutableLiveDataOf
import bobko.todomail.util.readPref
import bobko.todomail.util.writePref

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    val todoTextDraft = mutableLiveDataOf(TextFieldValue())

    init {
        application.readPref { PrefManager.todoDraft.value }.takeIf { it.isNotBlank() }?.let {
            prefillSharedText(it, null, useRawText = true)
        }
    }

    var todoTextDraftIsChangedAtLeastOnce = mutableLiveDataOf(false)

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

    fun prefillSharedText(
        sharedText: String,
        callerAppLabel: String?,
        useRawText: Boolean = false
    ) {
        require(sharedText.isNotBlank())
        if (todoTextDraft.value.text.isEmpty() &&
            !isPrefilledWithSharedText /* Allow to prefill TextField only once */) {
            isPrefilledWithSharedText = true
            todoTextDraft.value =
                if (useRawText) TextFieldValue(sharedText)
                else composeSharedText(sharedText, callerAppLabel)
        }
    }

    private fun composeSharedText(
        sharedText: String,
        callerAppLabel: String?
    ): TextFieldValue {
        val text = buildString {
            appendLine()
            appendLine()
            if (callerAppLabel != null &&
                LastUsedAppFeatureManager.isFeatureEnabled(getApplication<Application>()).value
            ) {
                appendLine("Shared from: $callerAppLabel")
            }
            append(sharedText)
        }
        return TextFieldValue(text)
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().writePref {
            PrefManager.todoDraft.value = todoTextDraft.value.text
        }
    }
}
