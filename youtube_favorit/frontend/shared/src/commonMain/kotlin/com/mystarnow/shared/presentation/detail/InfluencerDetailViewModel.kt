package com.mystarnow.shared.presentation.detail

import com.mystarnow.shared.core.model.ViewEvent
import com.mystarnow.shared.core.util.StateHolder
import com.mystarnow.shared.domain.model.InfluencerDetail
import com.mystarnow.shared.domain.usecase.GetInfluencerDetail
import com.mystarnow.shared.domain.usecase.ObserveFavorites
import com.mystarnow.shared.domain.usecase.ToggleFavorite
import com.mystarnow.shared.ui.components.ActivityCardUiModel
import com.mystarnow.shared.ui.components.ChannelUiModel
import com.mystarnow.shared.ui.components.ProfileUiModel
import com.mystarnow.shared.ui.components.ScheduleRowUiModel
import com.mystarnow.shared.ui.components.UiSectionState
import com.mystarnow.shared.ui.components.toStatusText
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

data class LiveStatusUiModel(
    val statusText: String,
    val liveTitle: String?,
    val startedAt: String?,
    val watchUrl: String?,
)

data class InfluencerDetailUiState(
    val isLoading: Boolean = true,
    val profile: UiSectionState<ProfileUiModel?> = UiSectionState.empty(null),
    val channels: UiSectionState<List<ChannelUiModel>> = UiSectionState.empty(emptyList()),
    val liveStatus: UiSectionState<LiveStatusUiModel?> = UiSectionState.empty(null),
    val recentActivities: UiSectionState<List<ActivityCardUiModel>> = UiSectionState.empty(emptyList()),
    val schedules: UiSectionState<List<ScheduleRowUiModel>> = UiSectionState.empty(emptyList()),
    val favoriteSlugs: Set<String> = emptySet(),
)

class InfluencerDetailViewModel(
    private val slug: String,
    private val getInfluencerDetail: GetInfluencerDetail,
    private val toggleFavorite: ToggleFavorite,
    observeFavorites: ObserveFavorites,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
) : StateHolder(dispatcher) {
    private val _state = MutableStateFlow(InfluencerDetailUiState())
    val state: StateFlow<InfluencerDetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ViewEvent>()
    val events: SharedFlow<ViewEvent> = _events.asSharedFlow()

    private var lastDetail: InfluencerDetail? = null

    init {
        scope.launch {
            observeFavorites().collect { favorites ->
                _state.update { it.copy(favoriteSlugs = favorites) }
                render(lastDetail, favorites)
            }
        }
        reload()
    }

    fun reload() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { getInfluencerDetail(slug) }
                .onSuccess {
                    lastDetail = it
                    render(it, _state.value.favoriteSlugs)
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            profile = UiSectionState.failed(null, "상세 정보를 불러오지 못했습니다."),
                        )
                    }
                    _events.emit(ViewEvent.Message("상세 화면 로딩에 실패했습니다."))
                }
        }
    }

    fun onToggleFavorite() {
        scope.launch { toggleFavorite(slug) }
    }

    private fun render(detail: InfluencerDetail?, favorites: Set<String>) {
        if (detail == null) return
        _state.value = InfluencerDetailUiState(
            isLoading = false,
            profile = detail.profile.toUiState { it.toUiModel(isFavorite = slug in favorites) },
            channels = detail.channels.toUiState { channels -> channels.map { it.toUiModel() } },
            liveStatus = detail.liveStatus.toUiState {
                LiveStatusUiModel(
                    statusText = it.toStatusText(),
                    liveTitle = it.liveTitle,
                    startedAt = it.startedAt,
                    watchUrl = it.watchUrl,
                )
            },
            recentActivities = detail.recentActivities.toUiState { it.items.map { activity -> activity.toUiModel() } },
            schedules = detail.schedules.toUiState { it.items.map { schedule -> schedule.toUiModel() } },
            favoriteSlugs = favorites,
        )
    }
}
