package com.mystarnow.shared.presentation

import com.mystarnow.shared.InMemoryFavoritesStorage
import com.mystarnow.shared.StaticApiProvider
import com.mystarnow.shared.data.mock.MockMyStarNowApi
import com.mystarnow.shared.data.repository.FavoritesRepositoryImpl
import com.mystarnow.shared.data.repository.HomeRepositoryImpl
import com.mystarnow.shared.data.repository.InfluencerRepositoryImpl
import com.mystarnow.shared.domain.model.Platform
import com.mystarnow.shared.domain.usecase.GetHomeFeed
import com.mystarnow.shared.domain.usecase.GetInfluencerDetail
import com.mystarnow.shared.domain.usecase.ObserveFavorites
import com.mystarnow.shared.domain.usecase.SearchInfluencers
import com.mystarnow.shared.domain.usecase.ToggleFavorite
import com.mystarnow.shared.presentation.detail.InfluencerDetailViewModel
import com.mystarnow.shared.presentation.favorites.FavoritesViewModel
import com.mystarnow.shared.presentation.home.HomeViewModel
import com.mystarnow.shared.presentation.list.InfluencerListViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelStateTest {
    private val dispatcher = StandardTestDispatcher()
    private val api = MockMyStarNowApi()
    private val apiProvider = StaticApiProvider(api)
    private val favoritesRepository = FavoritesRepositoryImpl(InMemoryFavoritesStorage())
    private val observeFavorites = ObserveFavorites(favoritesRepository)
    private val toggleFavorite = ToggleFavorite(favoritesRepository)

    @Test
    fun homeViewModelLoadsMockSections() = runTest(dispatcher) {
        val viewModel = HomeViewModel(
            getHomeFeed = GetHomeFeed(HomeRepositoryImpl(apiProvider)),
            toggleFavorite = toggleFavorite,
            observeFavorites = observeFavorites,
            dispatcher = dispatcher,
        )

        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.isLoading)
        assertEquals(1, viewModel.state.value.liveNow.value.size)
        assertEquals("partial", viewModel.state.value.latestUpdates.status.name.lowercase())
        assertEquals("stale", viewModel.state.value.latestUpdates.freshness.name.lowercase())
        assertTrue(viewModel.state.value.latestUpdates.message?.contains("Instagram") == true)
        viewModel.clear()
    }

    @Test
    fun listViewModelFiltersByPlatformAndQuery() = runTest(dispatcher) {
        val viewModel = InfluencerListViewModel(
            searchInfluencers = SearchInfluencers(InfluencerRepositoryImpl(apiProvider)),
            toggleFavorite = toggleFavorite,
            observeFavorites = observeFavorites,
            dispatcher = dispatcher,
        )

        advanceUntilIdle()
        viewModel.togglePlatform(Platform.YOUTUBE)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.results.value.all { "YouTube" in it.platforms })
        assertTrue(viewModel.state.value.filters.value.platforms.isNotEmpty())

        viewModel.updateQuery("준")
        viewModel.onSearch()
        advanceUntilIdle()
        assertEquals("준", viewModel.state.value.results.value.first().name)
        viewModel.loadMore()
        advanceUntilIdle()
        viewModel.clear()
    }

    @Test
    fun detailAndFavoritesReactToFavoriteChanges() = runTest(dispatcher) {
        val detailViewModel = InfluencerDetailViewModel(
            slug = "haru",
            getInfluencerDetail = GetInfluencerDetail(InfluencerRepositoryImpl(apiProvider)),
            toggleFavorite = toggleFavorite,
            observeFavorites = observeFavorites,
            dispatcher = dispatcher,
        )
        val favoritesViewModel = FavoritesViewModel(
            searchInfluencers = SearchInfluencers(InfluencerRepositoryImpl(apiProvider)),
            toggleFavorite = toggleFavorite,
            observeFavorites = observeFavorites,
            dispatcher = dispatcher,
        )

        advanceUntilIdle()
        detailViewModel.onToggleFavorite()
        advanceUntilIdle()

        assertTrue(detailViewModel.state.value.favoriteSlugs.contains("haru"))
        assertEquals(1, favoritesViewModel.state.value.items.size)
        assertTrue(detailViewModel.state.value.recentActivities.message?.isNotBlank() == true)

        detailViewModel.clear()
        favoritesViewModel.clear()
    }
}
