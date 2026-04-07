package de.mm20.launcher2.aiassistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class ActionDispatcher(private val context: Context) {

    /**
     * Returns a JSON schema string describing available tools for the AI system prompt.
     */
    fun toolDescriptions(): String = """
        Available tools (respond with JSON to invoke):
        - launch_app: {"tool":"launch_app","package":"<package_name>"}
        - search: {"tool":"search","query":"<query>"}
        - create_alarm: {"tool":"create_alarm","hour":<0-23>,"minute":<0-59>,"message":"<label>"}
        - open_settings: {"tool":"open_settings"}
        - web_search: {"tool":"web_search","query":"<query>"}
    """.trimIndent()

    /**
     * Attempts to parse [text] as a tool call JSON and dispatch the corresponding action.
     * Returns true if a tool call was dispatched.
     */
    fun tryDispatch(text: String): Boolean {
        val json = try {
            Json.parseToJsonElement(text.trim()) as? JsonObject ?: return false
        } catch (_: Exception) {
            return false
        }

        return when (json["tool"]?.jsonPrimitive?.content) {
            "launch_app" -> {
                val pkg = json["package"]?.jsonPrimitive?.content ?: return false
                val intent = context.packageManager.getLaunchIntentForPackage(pkg) ?: return false
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            }
            "search" -> {
                val query = json["query"]?.jsonPrimitive?.content ?: return false
                val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                    putExtra("query", query)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            }
            "create_alarm" -> {
                val hour = json["hour"]?.jsonPrimitive?.content?.toIntOrNull() ?: return false
                val minute = json["minute"]?.jsonPrimitive?.content?.toIntOrNull() ?: return false
                val message = json["message"]?.jsonPrimitive?.content
                val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(AlarmClock.EXTRA_HOUR, hour)
                    putExtra(AlarmClock.EXTRA_MINUTES, minute)
                    if (message != null) putExtra(AlarmClock.EXTRA_MESSAGE, message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            }
            "open_settings" -> {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            }
            "web_search" -> {
                val query = json["query"]?.jsonPrimitive?.content ?: return false
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://www.google.com/search?q=${Uri.encode(query)}"
                )).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            }
            else -> false
        }
    }
}
