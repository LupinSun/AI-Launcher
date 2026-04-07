package de.mm20.launcher2.ui.launcher.scaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun ProactiveSuggestionsPanel(modifier: Modifier = Modifier) {
    val viewModel: ProactiveSuggestionsPanelVM = viewModel()
    val enabled by viewModel.proactiveSuggestionsEnabled.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val context = LocalContext.current

    AnimatedVisibility(enabled && suggestions.isNotEmpty(), modifier = modifier) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(suggestions) { item ->
                SuggestionChip(
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = { item.searchable.launch(context, null) },
                    label = { Text(item.searchable.labelOverride ?: item.searchable.label) },
                )
            }
        }
    }
}
