package de.mm20.launcher2.aisuggestions

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import de.mm20.launcher2.preferences.ai.AiSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

interface ProactiveSuggestionsRepository {
    val suggestions: Flow<List<SuggestedItem>>
    fun scheduleRefresh(intervalMinutes: Int = 30)
    fun cancelRefresh()
}

internal class ProactiveSuggestionsRepositoryImpl(
    private val context: Context,
    private val aiSettings: AiSettings,
) : ProactiveSuggestionsRepository {

    private val prefs = ProactiveSuggestionsWorker.getPreferences(context)

    override val suggestions: Flow<List<SuggestedItem>> = flow {
        val raw = prefs.getString(ProactiveSuggestionsWorker.PREF_KEY_SUGGESTIONS, null)
        if (raw.isNullOrBlank()) {
            emit(emptyList())
        } else {
            try {
                emit(Json.decodeFromString(raw))
            } catch (_: Exception) {
                emit(emptyList())
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun scheduleRefresh(intervalMinutes: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<ProactiveSuggestionsWorker>(
            intervalMinutes.toLong(), TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ProactiveSuggestionsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    override fun cancelRefresh() {
        WorkManager.getInstance(context).cancelUniqueWork(ProactiveSuggestionsWorker.WORK_NAME)
    }
}
