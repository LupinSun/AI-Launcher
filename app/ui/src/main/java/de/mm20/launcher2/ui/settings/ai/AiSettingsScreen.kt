package de.mm20.launcher2.ui.settings.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import de.mm20.launcher2.preferences.AiProviderType
import de.mm20.launcher2.ui.component.preferences.ListPreference
import de.mm20.launcher2.ui.component.preferences.PreferenceCategory
import de.mm20.launcher2.ui.component.preferences.PreferenceScreen
import de.mm20.launcher2.ui.component.preferences.SwitchPreference
import de.mm20.launcher2.ui.component.preferences.TextPreference
import kotlinx.serialization.Serializable

@Serializable
data object AiSettingsRoute : NavKey

@Composable
fun AiSettingsScreen() {
    val viewModel: AiSettingsScreenVM = viewModel()

    val activeProvider by viewModel.activeProvider.collectAsState()
    val aiSearchEnabled by viewModel.aiSearchEnabled.collectAsState()
    val assistantEnabled by viewModel.assistantEnabled.collectAsState()
    val proactiveSuggestionsEnabled by viewModel.proactiveSuggestionsEnabled.collectAsState()

    PreferenceScreen(title = "AI Settings") {
        item {
            PreferenceCategory {
                ListPreference(
                    title = "AI Provider",
                    items = listOf(
                        "None" to AiProviderType.None,
                        "Anthropic (Claude)" to AiProviderType.Anthropic,
                        "Google Gemini" to AiProviderType.Gemini,
                        "OpenAI" to AiProviderType.OpenAI,
                        "Local (Ollama)" to AiProviderType.Local,
                    ),
                    value = activeProvider,
                    onValueChanged = { if (it != null) viewModel.setActiveProvider(it) },
                )
                AnimatedVisibility(activeProvider == AiProviderType.Anthropic) {
                    val key = viewModel.getApiKey(AiProviderType.Anthropic)
                    TextPreference(
                        title = "Anthropic API Key",
                        value = key,
                        summary = if (key.isBlank()) "Not set" else "••••••••" + key.takeLast(4),
                        onValueChanged = { viewModel.setApiKey(AiProviderType.Anthropic, it) },
                        placeholder = "sk-ant-…",
                    )
                }
                AnimatedVisibility(activeProvider == AiProviderType.Gemini) {
                    val key = viewModel.getApiKey(AiProviderType.Gemini)
                    TextPreference(
                        title = "Gemini API Key",
                        value = key,
                        summary = if (key.isBlank()) "Not set" else "••••••••" + key.takeLast(4),
                        onValueChanged = { viewModel.setApiKey(AiProviderType.Gemini, it) },
                        placeholder = "AIza…",
                    )
                }
                AnimatedVisibility(activeProvider == AiProviderType.OpenAI) {
                    val key = viewModel.getApiKey(AiProviderType.OpenAI)
                    TextPreference(
                        title = "OpenAI API Key",
                        value = key,
                        summary = if (key.isBlank()) "Not set" else "••••••••" + key.takeLast(4),
                        onValueChanged = { viewModel.setApiKey(AiProviderType.OpenAI, it) },
                        placeholder = "sk-…",
                    )
                }
            }
        }
        item {
            PreferenceCategory {
                SwitchPreference(
                    title = "AI Search",
                    summary = "Show AI-generated answers inline in search results",
                    value = aiSearchEnabled == true,
                    onValueChanged = { viewModel.setAiSearchEnabled(it) },
                )
                SwitchPreference(
                    title = "AI Assistant",
                    summary = "Enable conversational assistant accessible from the search bar",
                    value = assistantEnabled == true,
                    onValueChanged = { viewModel.setAssistantEnabled(it) },
                )
                SwitchPreference(
                    title = "Proactive Suggestions",
                    summary = "Show context-aware app suggestions on the home screen",
                    value = proactiveSuggestionsEnabled == true,
                    onValueChanged = { viewModel.setProactiveSuggestionsEnabled(it) },
                )
            }
        }
    }
}
