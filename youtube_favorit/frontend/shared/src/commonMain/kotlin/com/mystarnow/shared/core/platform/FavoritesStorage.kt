package com.mystarnow.shared.core.platform

import kotlinx.coroutines.flow.Flow

interface FavoritesStorage {
    fun observeFavorites(): Flow<Set<String>>
    suspend fun toggleFavorite(slug: String)
}
