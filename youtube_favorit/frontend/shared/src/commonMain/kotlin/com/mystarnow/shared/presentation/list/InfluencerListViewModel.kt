package com.mystarnow.shared.presentation.list

import com.mystarnow.shared.core.model.ViewEvent
import com.mystarnow.shared.core.util.StateHolder
import com.mystarnow.shared.domain.model.InfluencerListPage
import com.mystarnow.shared.domain.model.Platform
import com.mystarnow.shared.domain.model.SearchInfluencersQuery
import com.mystarnow.shared.domain.usecase.ObserveFavorites
import com.mystarnow.shared.domain.usecase.SearchInfluencers
import com.mystarnow.shared.domain.usecase.ToggleFavorite
import com.mystarnow.shared.ui.components.FilterBarUiModel
import com.mystarnow.shared.ui.components.ChipOptionUiModel
import com.mystarnow.shared.ui.components.InfluencerCardUiModel
import com.mystarnow.shared.ui.components.UiSectionState
import com.mystarnow.shared.ui.components.toUiModel
import com.mystarnow.shared.ui.components.toUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InfluencerListUiState(
    val query: String = "",
    val selectedCategory: String? = null,
    val selectedPlatforms: Set<Platform> = emptySet(),
    val sort: String = "featured",
    val isLoading: Boolean = true,
    val results: UiSectionState<List<InfluencerCardUiModel>> = UiSectionState.empty(emptyList()),
    val filters: UiSectionState<FilterBarUiModel> = UiSectionState.empty(FilterBarUiModel()),
    val favoriteSlugs: Set<String> = emptySet(),
    val nextCursor: String? = null,
    val hasNext: Boolean = false,
)

class InfluencerListViewModel(
    private val searchInfluencers: SearchInfluencers,
    private val toggleFavorite: ToggleFavorite,
    observeFavorites: ObserveFavorites,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
) : StateHolder(dispatcher) {
    private val _state = MutableStateFlow(InfluencerListUiState())
    val state: StateFlow<InfluencerListUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ViewEvent>()
    val events: SharedFlow<ViewEvent> = _events.asSharedFlow()

    private var lastPage: InfluencerListPage? = null

    init {
        scope.launch {
            observeFavorites().collect { favorites ->
                _state.update { it.copy(favoriteSlugs = favorites) }
                render(lastPage, favorites, append = false)
            }
        }
        reload()
    }

    fun updateQuery(value: String) {
        _state.update { it.copy(query = value) }
    }

    fun selectCategory(value: String?) {
        _state.update {
            it.copy(selectedCategory = if (it.selectedCategory == value) null else value)
        }
        reload()
    }

    fun togglePlatform(platform: Platform) {
        _state.update { state ->
            val next = state.selectedPlatforms.toMutableSet()
            if (!next.add(platform)) next.remove(platform)
            state.copy(selectedPlatforms = next)
        }
        reload()
    }

    fun updateSort(sort: String) {
        _state.update { it.copy(sort = sort) }
        reload()
    }

    fun onSearch() = reload()

    fun loadMore() {
        val current = _state.value
        if (!current.hasNext || current.nextCursor == null || current.isLoading) return
        fetch(cursor = current.nextCursor, append = true)
    }

    fun onToggleFavorite(slug: String) {
        scope.launch { toggleFavorite(slug) }
    }

    fun retry() = reload()

    private fun reload() {
        fetch(cursor = null, append = false)
    }

    private fun fetch(cursor: String?, append: Boolean) {
        val current = _state.value
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                searchInfluencers(
                    SearchInfluencersQuery(
                        query = current.query,
                        category = current.selectedCategory,
                        platforms = current.selectedPlatforms.toList(),
                        sort = current.sort,
                        cursor = cursor,
                    )
                )
            }.onSuccess { page ->
                lastPage = page
                render(page, _state.value.favoriteSlugs, append)
            }.onFailure {
                _state.update {
                    it.copy(
                        isLoading = false,
                        results = UiSectionState.failed(emptyList(), "인플루언서 목록을 불러오지 못했습니다."),
                    )
                }
                _events.emit(ViewEvent.Message("인플루언서 목록 로딩에 실패했습니다."))
            }
        }
    }

    private fun render(
        page: InfluencerListPage?,
        favorites: Set<String>,
        append: Boolean,
    ) {
        if (page == null) return
        val mappedResults = page.results.toUiState { results ->
            results.items.map { it.toUiModel(isFavorite = it.slug in favorites) }
        }
        val mergedResults = if (append) {
            mappedResults.copy(value = _state.value.results.value + mappedResults.value)
        } else {
            mappedResults
        }

        _state.value = _state.value.copy(
            isLoading = false,
            results = mergedResults,
            filters = page.filters.toUiState { filterOptions ->
                FilterBarUiModel(
                    categories = filterOptions.categories.map {
                        ChipOptionUiModel(it.value, it.label, it.enabled)
                    },
                    platforms = filterOptions.platforms.map {
                        ChipOptionUiModel(it.value, it.label, it.enabled)
                    },
                    sortOptions = filterOptions.sortOptions.map {
                        ChipOptionUiModel(it.value, it.label, it.enabled)
                    },
                )
            },
            favoriteSlugs = favorites,
            nextCursor = page.results.data.pageInfo.nextCursor,
            hasNext = page.results.data.pageInfo.hasNext,
        )
    }
}
