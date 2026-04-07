package de.mm20.launcher2.aisuggestions

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val aiSuggestionsModule = module {
    single<ProactiveSuggestionsRepository> {
        ProactiveSuggestionsRepositoryImpl(androidContext(), get())
    }
}
