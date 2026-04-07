package com.mystarnow.shared.presentation.home

import com.mystarnow.shared.core.model.ViewEvent
import com.mystarnow.shared.core.util.StateHolder
import com.mystarnow.shared.domain.model.HomeFeed
import com.mystarnow.shared.domain.usecase.GetHomeFeed
import com.mystarnow.shared.domain.usecase.ObserveFavorites
import com.mystarnow.shared.domain.usecase.ToggleFavorite
import com.mystarnow.shared.ui.components.SectionDebugUiModel
import com.mystarnow.shared.ui.components.ActivityCardUiModel
import com.mystarnow.shared.ui.components.InfluencerCardUiModel
import com.mystarnow.shared.ui.components.LiveNowUiModel
import com.mystarnow.shared.ui.components.ScheduleRowUiModel
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

data class HomeUiState(
    val isLoading: Boolean = true,
    val liveNow: UiSectionState<List<LiveNowUiModel>> = UiSectionState.empty(emptyList()),
    val latestUpdates: UiSectionState<List<ActivityCardUiModel>> = UiSectionState.empty(emptyList()),
    val todaySchedules: UiSectionState<List<ScheduleRowUiModel>> = UiSectionState.empty(emptyList()),
    val featuredInfluencers: UiSectionState<List<InfluencerCardUiModel>> = UiSectionState.empty(emptyList()),
    val favoriteSlugs: Set<String> = emptySet(),
    val debugInfo: SectionDebugUiModel? = null,
)

class HomeViewModel(
    private val getHomeFeed: GetHomeFeed,
    private val toggleFavorite: ToggleFavorite,
    observeFavorites: ObserveFavorites,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
) : StateHolder(dispatcher) {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ViewEvent>()
    val events: SharedFlow<ViewEvent> = _events.asSharedFlow()

    private var lastFeed: HomeFeed? = null

    init {
        scope.launch {
            observeFavorites().collect { favorites ->
                _state.update { current ->
                    current.copy(favoriteSlugs = favorites)
                }
                render(lastFeed, favorites)
            }
        }
        refresh()
    }

    fun refresh() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { getHomeFeed() }
                .onSuccess { feed ->
                    lastFeed = feed
                    render(feed, _state.value.favoriteSlugs)
                }
                .onFailure {
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            liveNow = UiSectionState.failed(emptyList(), "홈 데이터를 불러오지 못했습니다."),
                            latestUpdates = UiSectionState.failed(emptyList(), "최신 업데이트를 불러오지 못했습니다."),
                            todaySchedules = UiSectionState.failed(emptyList(), "일정을 불러오지 못했습니다."),
                            featuredInfluencers = UiSectionState.failed(emptyList(), "추천 인플루언서를 불러오지 못했습니다."),
                        )
                    }
                    _events.emit(ViewEvent.Message("홈 피드 로딩에 실패했습니다."))
                }
        }
    }

    fun onToggleFavorite(slug: String) {
        scope.launch {
            toggleFavorite(slug)
        }
    }

    private fun render(feed: HomeFeed?, favorites: Set<String>) {
        if (feed == null) return
        _state.value = HomeUiState(
            isLoading = false,
            liveNow = feed.liveNow.toUiState { section -> section.items.map { it.toUiModel() } },
            latestUpdates = feed.latestUpdates.toUiState { section -> section.items.map { it.toUiModel() } },
            todaySchedules = feed.todaySchedules.toUiState { section -> section.items.map { it.toUiModel() } },
            featuredInfluencers = feed.featuredInfluencers.toUiState { section ->
                section.items.map { it.toUiModel(isFavorite = it.slug in favorites) }
            },
            favoriteSlugs = favorites,
            debugInfo = SectionDebugUiModel(
                requestId = feed.meta.requestId,
                generatedAt = feed.meta.generatedAt,
                partialFailure = feed.meta.partialFailure,
                summary = buildString {
                    append("liveNow=${feed.liveNow.status.name.lowercase()}, ")
                    append("latestUpdates=${feed.latestUpdates.status.name.lowercase()}, ")
                    append("todaySchedules=${feed.todaySchedules.status.name.lowercase()}, ")
                    append("featuredInfluencers=${feed.featuredInfluencers.status.name.lowercase()}")
                },
            ),
        )
    }
}
