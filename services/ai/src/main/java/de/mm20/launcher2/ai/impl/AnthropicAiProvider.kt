package de.mm20.launcher2.ai.impl

import de.mm20.launcher2.ai.AiKeyStore
import de.mm20.launcher2.ai.AiMessage
import de.mm20.launcher2.ai.AiProvider
import de.mm20.launcher2.ai.apiName
import de.mm20.launcher2.preferences.AiProviderType
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.*

class AnthropicAiProvider(
    private val httpClient: HttpClient,
    private val keyStore: AiKeyStore,
) : AiProvider {

    override val isConfigured: Boolean
        get() = keyStore.getKey(AiProviderType.Anthropic).isNotBlank()

    override suspend fun chat(messages: List<AiMessage>): Flow<String> = flow {
        val apiKey = keyStore.getKey(AiProviderType.Anthropic)
        if (apiKey.isBlank()) return@flow

        val requestBody = buildJsonObject {
            put("model", "claude-3-5-sonnet-20241022")
            put("max_tokens", 1024)
            put("stream", true)
            putJsonArray("messages") {
                messages.filter { it.role != AiMessage.Role.System }.forEach { msg ->
                    addJsonObject {
                        put("role", msg.role.apiName)
                        put("content", msg.content)
                    }
                }
            }
            // System message as top-level field
            messages.firstOrNull { it.role == AiMessage.Role.System }?.let {
                put("system", it.content)
            }
        }

        val response: HttpResponse = httpClient.post("https://api.anthropic.com/v1/messages") {
            header("x-api-key", apiKey)
            header("anthropic-version", "2023-06-01")
            contentType(ContentType.Application.Json)
            setBody(requestBody.toString())
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw Exception("AI API error ${response.status.value}: $errorBody")
        }

        val channel: ByteReadChannel = response.bodyAsChannel()
        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line() ?: break
            if (line.startsWith("data: ")) {
                val data = line.removePrefix("data: ")
                if (data == "[DONE]") break
                try {
                    val json = Json.parseToJsonElement(data).jsonObject
                    val type = json["type"]?.jsonPrimitive?.content
                    if (type == "content_block_delta") {
                        val delta = json["delta"]?.jsonObject
                        val text = delta?.get("text")?.jsonPrimitive?.content
                        if (!text.isNullOrEmpty()) emit(text)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    override suspend fun classify(text: String, labels: List<String>): String? {
        val result = StringBuilder()
        chat(listOf(
            AiMessage(AiMessage.Role.System, "Classify the input into exactly one of: ${labels.joinToString(", ")}. Respond with only the label."),
            AiMessage(AiMessage.Role.User, text),
        )).collect { result.append(it) }
        return labels.firstOrNull { result.toString().trim().equals(it, ignoreCase = true) }
    }
}
