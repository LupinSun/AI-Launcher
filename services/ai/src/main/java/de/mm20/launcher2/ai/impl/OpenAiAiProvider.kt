package de.mm20.launcher2.ai.impl

import de.mm20.launcher2.ai.AiKeyStore
import de.mm20.launcher2.ai.AiMessage
import de.mm20.launcher2.ai.AiProvider
import de.mm20.launcher2.preferences.AiProviderType
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.*

class OpenAiAiProvider(
    private val httpClient: HttpClient,
    private val keyStore: AiKeyStore,
    private val baseUrl: String = "https://api.openai.com",
) : AiProvider {

    override val isConfigured: Boolean
        get() = keyStore.getKey(AiProviderType.OpenAI).isNotBlank()

    override suspend fun chat(messages: List<AiMessage>): Flow<String> = flow {
        val apiKey = keyStore.getKey(AiProviderType.OpenAI)
        if (apiKey.isBlank()) return@flow

        val requestBody = buildJsonObject {
            put("model", "gpt-4o-mini")
            put("stream", true)
            putJsonArray("messages") {
                messages.forEach { msg ->
                    addJsonObject {
                        put("role", msg.role.name)
                        put("content", msg.content)
                    }
                }
            }
        }

        val response: HttpResponse = httpClient.post("$baseUrl/v1/chat/completions") {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(requestBody.toString())
        }

        val channel: ByteReadChannel = response.bodyAsChannel()
        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line() ?: break
            if (line.startsWith("data: ")) {
                val data = line.removePrefix("data: ")
                if (data == "[DONE]") break
                try {
                    val json = Json.parseToJsonElement(data).jsonObject
                    val text = json["choices"]?.jsonArray
                        ?.firstOrNull()?.jsonObject
                        ?.get("delta")?.jsonObject
                        ?.get("content")?.jsonPrimitive?.content
                    if (!text.isNullOrEmpty()) emit(text)
                } catch (_: Exception) {}
            }
        }
    }

    override suspend fun classify(text: String, labels: List<String>): String? {
        val result = StringBuilder()
        chat(listOf(
            AiMessage(AiMessage.Role.system, "Classify the input into exactly one of: ${labels.joinToString(", ")}. Respond with only the label."),
            AiMessage(AiMessage.Role.user, text),
        )).collect { result.append(it) }
        return labels.firstOrNull { result.toString().trim().equals(it, ignoreCase = true) }
    }
}
