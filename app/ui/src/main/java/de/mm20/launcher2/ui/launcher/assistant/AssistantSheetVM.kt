package de.mm20.launcher2.ui.launcher.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mm20.launcher2.ai.AiMessage
import de.mm20.launcher2.aiassistant.AssistantRepository
import de.mm20.launcher2.aiassistant.AssistantState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AssistantSheetVM : ViewModel(), KoinComponent {

    private val repository: AssistantRepository by inject()

    val messages: StateFlow<List<AiMessage>> = repository.currentConversation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val state: StateFlow<AssistantState> = repository.state

    fun sendMessage(text: String) {
        viewModelScope.launch {
            repository.sendMessage(text)
        }
    }

    fun startNewConversation() {
        viewModelScope.launch {
            repository.startNewConversation()
        }
    }
}
