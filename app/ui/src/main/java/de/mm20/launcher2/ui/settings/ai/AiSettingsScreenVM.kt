package de.mm20.launcher2.ui.settings.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mm20.launcher2.ai.AiKeyStore
import de.mm20.launcher2.ai.AiProviderManager
import de.mm20.launcher2.preferences.AiProviderType
import de.mm20.launcher2.preferences.ai.AiSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AiSettingsScreenVM : ViewModel(), KoinComponent {
    private val aiSettings: AiSettings by inject()
    private val aiKeyStore: AiKeyStore by inject()
    private val aiProviderManager: AiProviderManager by inject()

    val activeProvider = aiSettings.activeProvider
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AiProviderType.None)

    val aiSearchEnabled = aiSettings.aiSearchEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val assistantEnabled = aiSettings.assistantEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val proactiveSuggestionsEnabled = aiSettings.proactiveSuggestionsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun setActiveProvider(type: AiProviderType) {
        viewModelScope.launch { aiSettings.setActiveProvider(type) }
    }

    fun setAiSearchEnabled(enabled: Boolean) {
        viewModelScope.launch { aiSettings.setAiSearchEnabled(enabled) }
    }

    fun setAssistantEnabled(enabled: Boolean) {
        viewModelScope.launch { aiSettings.setAssistantEnabled(enabled) }
    }

    fun setProactiveSuggestionsEnabled(enabled: Boolean) {
        viewModelScope.launch { aiSettings.setProactiveSuggestionsEnabled(enabled) }
    }

    fun getApiKey(type: AiProviderType): String {
        return aiKeyStore.getKey(type) ?: ""
    }

    fun setApiKey(type: AiProviderType, key: String) {
        aiProviderManager.setApiKey(type, key)
    }
}
