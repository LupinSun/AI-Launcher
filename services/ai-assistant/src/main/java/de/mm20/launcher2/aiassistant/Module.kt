package de.mm20.launcher2.aiassistant

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val aiAssistantModule = module {
    single { ContextBuilder(androidContext(), get()) }
    single { ActionDispatcher(androidContext()) }
    single<AssistantRepository> {
        AssistantRepositoryImpl(
            database = get(),
            providerManager = get(),
            contextBuilder = get(),
            actionDispatcher = get(),
        )
    }
}
