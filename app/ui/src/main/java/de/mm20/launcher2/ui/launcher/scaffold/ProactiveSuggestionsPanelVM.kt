package de.mm20.launcher2.ui.launcher.scaffold

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mm20.launcher2.aisuggestions.ProactiveSuggestionsRepository
import de.mm20.launcher2.preferences.ai.AiSettings
import de.mm20.launcher2.search.SavableSearchable
import de.mm20.launcher2.searchable.PinnedLevel
import de.mm20.launcher2.services.favorites.FavoritesService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ProactiveSuggestionsPanelVM : ViewModel(), KoinComponent {

    private val suggestionsRepository: ProactiveSuggestionsRepository by inject()
    private val favoritesService: FavoritesService by inject()
    private val aiSettings: AiSettings by inject()

    val proactiveSuggestionsEnabled = aiSettings.proactiveSuggestionsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    init {
        // Schedule or cancel the refresh worker based on settings
        aiSettings.proactiveSuggestionsEnabled
            .distinctUntilChanged()
            .onEach { enabled ->
                if (enabled) suggestionsRepository.scheduleRefresh()
                else suggestionsRepository.cancelRefresh()
            }
            .launchIn(viewModelScope)
    }

    data class SuggestionWithApp(
        val searchable: SavableSearchable,
        val reason: String,
    )

    val suggestions = combine(
        suggestionsRepository.suggestions,
        favoritesService.getFavorites(
            includeTypes = listOf("app"),
            minPinnedLevel = PinnedLevel.FrequentlyUsed,
            limit = 50,
        ),
    ) { suggestedItems, favorites ->
        val favsByKey = favorites.associateBy { it.key }
        suggestedItems.mapNotNull { item ->
            val app = favsByKey[item.key] ?: return@mapNotNull null
            SuggestionWithApp(searchable = app, reason = item.reason)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
}
