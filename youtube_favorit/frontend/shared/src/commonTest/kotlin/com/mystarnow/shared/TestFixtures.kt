package com.mystarnow.shared

import com.mystarnow.shared.core.model.AppMode
import com.mystarnow.shared.core.model.DeveloperSettings
import com.mystarnow.shared.core.platform.FavoritesStorage
import com.mystarnow.shared.core.platform.DeveloperSettingsStorage
import com.mystarnow.shared.data.remote.MyStarNowApi
import com.mystarnow.shared.data.remote.MyStarNowApiProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryFavoritesStorage : FavoritesStorage {
    private val favorites = MutableStateFlow<Set<String>>(emptySet())

    override fun observeFavorites(): Flow<Set<String>> = favorites.asStateFlow()

    override suspend fun toggleFavorite(slug: String) {
        favorites.value = favorites.value.toMutableSet().also { next ->
            if (!next.add(slug)) {
                next.remove(slug)
            }
        }
    }
}

class InMemoryDeveloperSettingsStorage(
    initial: DeveloperSettings = DeveloperSettings(),
) : DeveloperSettingsStorage {
    private val settings = MutableStateFlow(initial)

    override fun observeSettings(): Flow<DeveloperSettings> = settings.asStateFlow()

    override suspend fun updateMode(mode: AppMode) {
        settings.value = settings.value.copy(mode = mode)
    }

    override suspend fun updateBaseUrl(baseUrl: String) {
        settings.value = settings.value.copy(baseUrl = baseUrl)
    }
}

class StaticApiProvider(
    private val api: MyStarNowApi,
) : MyStarNowApiProvider {
    override suspend fun getApi(): MyStarNowApi = api
}
