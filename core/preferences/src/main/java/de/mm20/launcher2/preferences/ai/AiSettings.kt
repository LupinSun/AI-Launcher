package de.mm20.launcher2.preferences.ai

import de.mm20.launcher2.preferences.AiProviderType
import de.mm20.launcher2.preferences.LauncherDataStore
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class AiSettings internal constructor(private val dataStore: LauncherDataStore) {
    val activeProvider get() = dataStore.data.map { it.aiActiveProvider }.distinctUntilChanged()
    val activeModel get() = dataStore.data.map { it.aiActiveModel }.distinctUntilChanged()
    val aiSearchEnabled get() = dataStore.data.map { it.aiSearchEnabled }.distinctUntilChanged()
    val assistantEnabled get() = dataStore.data.map { it.assistantEnabled }.distinctUntilChanged()
    val proactiveSuggestionsEnabled get() = dataStore.data.map { it.proactiveSuggestionsEnabled }.distinctUntilChanged()
    val proactiveSuggestionsIntervalMinutes get() = dataStore.data.map { it.proactiveSuggestionsIntervalMinutes }.distinctUntilChanged()

    suspend fun setActiveProvider(type: AiProviderType) = dataStore.update { it.copy(aiActiveProvider = type) }
    suspend fun setActiveModel(model: String) = dataStore.update { it.copy(aiActiveModel = model) }
    suspend fun setAiSearchEnabled(enabled: Boolean) = dataStore.update { it.copy(aiSearchEnabled = enabled) }
    suspend fun setAssistantEnabled(enabled: Boolean) = dataStore.update { it.copy(assistantEnabled = enabled) }
    suspend fun setProactiveSuggestionsEnabled(enabled: Boolean) = dataStore.update { it.copy(proactiveSuggestionsEnabled = enabled) }
    suspend fun setProactiveSuggestionsIntervalMinutes(minutes: Int) = dataStore.update { it.copy(proactiveSuggestionsIntervalMinutes = minutes) }
}
