package de.mm20.launcher2.aisearch

import de.mm20.launcher2.search.SearchableRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val aiSearchModule = module {
    single<SearchableRepository<AiSearchResult>>(named<AiSearchResult>()) {
        AiSearchRepository(get(), get())
    }
}
