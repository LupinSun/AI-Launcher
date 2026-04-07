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

class GeminiAiProvider(
    private val httpClient: HttpClient,
    private val keyStore: AiKeyStore,
) : AiProvider {

    override val isConfigured: Boolean
        get() = keyStore.getKey(AiProviderType.Gemini).isNotBlank()

    override suspend fun chat(messages: List<AiMessage>): Flow<String> = flow {
        val apiKey = keyStore.getKey(AiProviderType.Gemini)
        if (apiKey.isBlank()) return@flow

        val model = "gemini-1.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?key=$apiKey&alt=sse"

        val contents = buildJsonArray {
            messages.filter { it.role != AiMessage.Role.system }.forEach { msg ->
                addJsonObject {
                    put("role", if (msg.role == AiMessage.Role.user) "user" else "model")
                    putJsonArray("parts") {
                        addJsonObject { put("text", msg.content) }
                    }
                }
            }
        }

        val requestBody = buildJsonObject {
            put("contents", contents)
            messages.firstOrNull { it.role == AiMessage.Role.system }?.let { sys ->
                putJsonObject("systemInstruction") {
                    putJsonArray("parts") {
                        addJsonObject { put("text", sys.content) }
                    }
                }
            }
        }

        val response: HttpResponse = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(requestBody.toString())
        }

        val channel: ByteReadChannel = response.bodyAsChannel()
        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line() ?: break
            if (line.startsWith("data: ")) {
                val data = line.removePrefix("data: ")
                try {
                    val json = Json.parseToJsonElement(data).jsonObject
                    val candidates = json["candidates"]?.jsonArray
                    val text = candidates?.firstOrNull()?.jsonObject
                        ?.get("content")?.jsonObject
                        ?.get("parts")?.jsonArray
                        ?.firstOrNull()?.jsonObject
                        ?.get("text")?.jsonPrimitive?.content
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
