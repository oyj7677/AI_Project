package com.mystarnow.shared.data.repository

import com.mystarnow.shared.InMemoryFavoritesStorage
import com.mystarnow.shared.domain.usecase.ObserveFavorites
import com.mystarnow.shared.domain.usecase.ToggleFavorite
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesPersistenceTest {
    @Test
    fun togglesFavoritesInStorage() = runTest {
        val repository = FavoritesRepositoryImpl(InMemoryFavoritesStorage())
        val observeFavorites = ObserveFavorites(repository)
        val toggleFavorite = ToggleFavorite(repository)

        toggleFavorite("haru")
        assertEquals(setOf("haru"), observeFavorites().first())

        toggleFavorite("haru")
        assertEquals(emptySet(), observeFavorites().first())
    }
}
