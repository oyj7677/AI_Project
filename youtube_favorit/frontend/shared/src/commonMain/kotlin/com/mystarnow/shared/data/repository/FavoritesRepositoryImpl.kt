package com.mystarnow.shared.data.repository

import com.mystarnow.shared.core.platform.FavoritesStorage
import com.mystarnow.shared.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class FavoritesRepositoryImpl(
    private val storage: FavoritesStorage,
) : FavoritesRepository {
    override fun observeFavorites(): Flow<Set<String>> = storage.observeFavorites()

    override suspend fun toggleFavorite(slug: String) {
        storage.toggleFavorite(slug)
    }
}
