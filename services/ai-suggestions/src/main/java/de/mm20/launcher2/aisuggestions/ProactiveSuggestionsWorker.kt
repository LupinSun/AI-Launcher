package de.mm20.launcher2.aisuggestions

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.mm20.launcher2.ai.AiMessage
import de.mm20.launcher2.ai.AiProviderManager
import de.mm20.launcher2.searchable.SavableSearchableRepository
import de.mm20.launcher2.searchable.VisibilityLevel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProactiveSuggestionsWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    private val aiProviderManager: AiProviderManager by inject()
    private val searchableRepository: SavableSearchableRepository by inject()

    override suspend fun doWork(): Result {
        val provider = aiProviderManager.activeProvider.first()
        if (!provider.isConfigured) return Result.success()

        // Get top-used apps (by weight descending)
        val topApps = searchableRepository.getKeys(
            maxVisibility = VisibilityLevel.SearchOnly,
            includeTypes = listOf("app"),
        ).first().take(20)

        val weights = searchableRepository.getWeights(topApps).first()
        val rankedApps = topApps
            .sortedByDescending { weights[it] ?: 0.0 }
            .take(10)

        if (rankedApps.isEmpty()) return Result.success()

        val dateStr = SimpleDateFormat("EEEE, MMMM d yyyy, HH:mm", Locale.getDefault())
            .format(Date())

        val systemPrompt = """
            You are a proactive assistant for a launcher. Based on the user's app usage patterns,
            suggest up to 3 apps they are most likely to open next.

            Current date and time: $dateStr

            Frequently used apps (keys): ${rankedApps.joinToString(", ")}

            Respond ONLY with a JSON array of objects, no explanation. Each object must have:
            - "key": the app key from the list above
            - "reason": a short (max 5 words) reason why

            Example: [{"key":"app://com.example","reason":"Morning commute time"}]
        """.trimIndent()

        return try {
            val responseFlow = provider.chat(
                listOf(
                    AiMessage(AiMessage.Role.System, systemPrompt),
                    AiMessage(AiMessage.Role.User, "What should I open now?"),
                )
            )
            val fullResponse = StringBuilder()
            responseFlow.collect { token -> fullResponse.append(token) }

            val json = fullResponse.toString().let { raw ->
                val start = raw.indexOf('[')
                val end = raw.lastIndexOf(']')
                if (start >= 0 && end > start) raw.substring(start, end + 1) else null
            } ?: return Result.success()

            val parsed = Json.parseToJsonElement(json) as? JsonArray ?: return Result.success()
            val suggestions = parsed.mapNotNull { element ->
                val obj = element.jsonObject
                val key = obj["key"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val reason = obj["reason"]?.jsonPrimitive?.content ?: return@mapNotNull null
                if (key in rankedApps) SuggestedItem(key = key, reason = reason) else null
            }

            if (suggestions.isNotEmpty()) {
                getPreferences(applicationContext).edit()
                    .putString(PREF_KEY_SUGGESTIONS, Json.encodeToString(suggestions))
                    .apply()
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("ProactiveSuggestions", "Failed to fetch suggestions", e)
            Result.retry()
        }
    }

    companion object {
        const val PREF_KEY_SUGGESTIONS = "suggestions"
        const val WORK_NAME = "ProactiveSuggestions"

        fun getPreferences(context: Context): SharedPreferences =
            context.getSharedPreferences("ai_suggestions", Context.MODE_PRIVATE)
    }
}
