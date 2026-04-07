package de.mm20.launcher2.aiassistant

import android.app.usage.UsageStatsManager
import android.content.Context
import de.mm20.launcher2.calendar.CalendarRepository
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class ContextBuilder(
    private val context: Context,
    private val calendarRepository: CalendarRepository,
) {
    suspend fun buildSystemContext(): String {
        val sb = StringBuilder()

        // Date/time
        val dateFormat = SimpleDateFormat("EEEE, MMMM d yyyy, HH:mm", Locale.getDefault())
        sb.appendLine("Current date and time: ${dateFormat.format(Date())}")

        // Foreground app (last 1 min)
        val foregroundApp = getForegroundApp()
        if (foregroundApp != null) {
            sb.appendLine("User is currently using: $foregroundApp")
        }

        // Upcoming calendar events (top 3)
        val now = System.currentTimeMillis()
        val events = calendarRepository.findMany(
            from = now,
            to = now + 7 * 24 * 60 * 60 * 1000L,
        ).firstOrNull()?.take(3)

        if (!events.isNullOrEmpty()) {
            sb.appendLine("Upcoming calendar events:")
            val eventFmt = SimpleDateFormat("EEE HH:mm", Locale.getDefault())
            events.forEach { event ->
                sb.appendLine("  - ${event.label} at ${eventFmt.format(Date(event.startTime))}")
            }
        }

        return sb.toString().trimEnd()
    }

    private fun getForegroundApp(): String? {
        return try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 60_000L, now)
            stats?.maxByOrNull { it.lastTimeUsed }?.packageName
                ?.let { pkg -> context.packageManager.getApplicationLabel(
                    context.packageManager.getApplicationInfo(pkg, 0)
                ).toString() }
        } catch (_: Exception) {
            null
        }
    }
}
