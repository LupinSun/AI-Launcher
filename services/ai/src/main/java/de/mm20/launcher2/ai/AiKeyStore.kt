package de.mm20.launcher2.ai

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import de.mm20.launcher2.preferences.AiProviderType

class AiKeyStore(private val context: Context) {
    private val prefs by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "ai_keys",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (e: Exception) {
            // Fall back to clearing keys if keystore is unavailable
            context.getSharedPreferences("ai_keys_fallback", Context.MODE_PRIVATE)
        }
    }

    fun getKey(provider: AiProviderType): String = prefs.getString(provider.name, "") ?: ""
    fun setKey(provider: AiProviderType, key: String) {
        prefs.edit().putString(provider.name, key).apply()
    }
    fun clearKey(provider: AiProviderType) {
        prefs.edit().remove(provider.name).apply()
    }
}
