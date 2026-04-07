package com.mystarnow.shared.presentation.favorites

import com.mystarnow.shared.core.model.ViewEvent
import com.mystarnow.shared.core.util.StateHolder
import com.mystarnow.shared.domain.model.SearchInfluencersQuery
import com.mystarnow.shared.domain.usecase.ObserveFavorites
import com.mystarnow.shared.domain.usecase.SearchInfluencers
import com.mystarnow.shared.domain.usecase.ToggleFavorite
import com.mystarnow.shared.ui.components.InfluencerCardUiModel
import com.mystarnow.shared.ui.components.toUiModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val items: List<InfluencerCardUiModel> = emptyList(),
    val favoriteSlugs: Set<String> = emptySet(),
)

class FavoritesViewModel(
    private val searchInfluencers: SearchInfluencers,
    private val toggleFavorite: ToggleFavorite,
    observeFavorites: ObserveFavorites,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
) : StateHolder(dispatcher) {
    private val _state = MutableStateFlow(FavoritesUiState())
    val state: StateFlow<FavoritesUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ViewEvent>()
    val events: SharedFlow<ViewEvent> = _events.asSharedFlow()

    init {
        scope.launch {
            observeFavorites().collect { favorites ->
                loadFavorites(favorites)
            }
        }
    }

    fun onToggleFavorite(slug: String) {
        scope.launch { toggleFavorite(slug) }
    }

    fun retry() {
        loadFavorites(_state.value.favoriteSlugs)
    }

    private fun loadFavorites(favorites: Set<String>) {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, favoriteSlugs = favorites)
            if (favorites.isEmpty()) {
                _state.value = FavoritesUiState(isLoading = false, items = emptyList(), favoriteSlugs = emptySet())
                return@launch
            }
            runCatching {
                searchInfluencers(SearchInfluencersQuery(limit = 50))
            }.onSuccess { page ->
                _state.value = FavoritesUiState(
                    isLoading = false,
                    items = page.results.data.items
                        .filter { it.slug in favorites }
                        .map { it.toUiModel(isFavorite = true) },
                    favoriteSlugs = favorites,
                )
            }.onFailure {
                _state.value = FavoritesUiState(isLoading = false, items = emptyList(), favoriteSlugs = favorites)
                _events.emit(ViewEvent.Message("즐겨찾기 목록을 불러오지 못했습니다."))
            }
        }
    }
}
