package de.mm20.launcher2.ai

import de.mm20.launcher2.ai.impl.AnthropicAiProvider
import de.mm20.launcher2.ai.impl.GeminiAiProvider
import de.mm20.launcher2.ai.impl.LocalAiProvider
import de.mm20.launcher2.ai.impl.OpenAiAiProvider
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val AiHttpClientQualifier = named("aiHttpClient")

val aiModule = module {
    single(AiHttpClientQualifier) {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
    single { AiKeyStore(androidContext()) }
    single { AnthropicAiProvider(get(AiHttpClientQualifier), get()) }
    single { GeminiAiProvider(get(AiHttpClientQualifier), get()) }
    single { OpenAiAiProvider(get(AiHttpClientQualifier), get()) }
    single { LocalAiProvider() }
    single { AiProviderManager(get(), get(), get(), get(), get(), get()) }
}
