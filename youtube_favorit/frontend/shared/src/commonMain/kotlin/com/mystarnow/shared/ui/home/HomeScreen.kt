package com.mystarnow.shared.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mystarnow.shared.core.model.AppMode
import com.mystarnow.shared.presentation.home.HomeUiState
import com.mystarnow.shared.ui.components.ActivityCard
import com.mystarnow.shared.ui.components.AppFeatureFlagsUiModel
import com.mystarnow.shared.ui.components.DeveloperPanel
import com.mystarnow.shared.ui.components.DeveloperSettingsUiModel
import com.mystarnow.shared.ui.components.EmptyStateCard
import com.mystarnow.shared.ui.components.InfluencerCard
import com.mystarnow.shared.ui.components.SectionStateSummary
import com.mystarnow.shared.ui.components.ScheduleCard
import com.mystarnow.shared.ui.components.SectionHeader

@Composable
fun HomeScreen(
    state: HomeUiState,
    settings: DeveloperSettingsUiModel,
    featureFlags: AppFeatureFlagsUiModel,
    onRefresh: () -> Unit,
    onOpenInfluencer: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onModeChange: (AppMode) -> Unit,
    onBaseUrlChange: (String) -> Unit,
    onOpenExternalLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasContent = state.liveNow.value.isNotEmpty() ||
        state.latestUpdates.value.isNotEmpty() ||
        state.todaySchedules.value.isNotEmpty() ||
        state.featuredInfluencers.value.isNotEmpty()

    if (state.isLoading && !hasContent) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
            Text("MyStarNow 홈을 불러오는 중입니다.")
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SectionHeader("MyStarNow", "YouTube + Instagram 1차 MVP")
        DeveloperPanel(
            settings = settings,
            featureFlags = featureFlags,
            debugInfo = state.debugInfo,
            onModeChange = onModeChange,
            onBaseUrlChange = onBaseUrlChange,
            onRefresh = onRefresh,
        )
        if (state.isLoading) {
            Text(
                "최신 데이터를 새로고침하는 중입니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedButton(onClick = onRefresh) {
            Text("새로고침")
        }

        if (featureFlags.enableLiveNow) {
            SectionHeader("지금 라이브 중")
            SectionStateSummary(state.liveNow)
            if (state.liveNow.value.isEmpty()) {
                EmptyStateCard("라이브 없음", "현재 방송 중인 인플루언서가 없습니다.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.liveNow.value.forEach { item ->
                        EmptyStateCard(
                            title = "${item.name} · ${item.platformLabel}",
                            description = item.liveTitle,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        item.watchUrl?.let { url ->
                            OutlinedButton(onClick = { onOpenExternalLink(url) }) {
                                Text("시청하기")
                            }
                        }
                    }
                }
            }
        }

        if (featureFlags.showRecentActivities) {
            SectionHeader("최신 업데이트", state.latestUpdates.message)
            SectionStateSummary(state.latestUpdates)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.latestUpdates.value.isEmpty()) {
                    EmptyStateCard("업데이트 없음", "최근 활동이 아직 없습니다.")
                } else {
                    state.latestUpdates.value.forEach { item ->
                        ActivityCard(item = item, onOpen = { onOpenExternalLink(item.externalUrl) })
                    }
                }
            }
        }

        if (featureFlags.showSchedules) {
            SectionHeader("오늘 예정 방송")
            SectionStateSummary(state.todaySchedules)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.todaySchedules.value.isEmpty()) {
                    EmptyStateCard("일정 없음", "오늘 예정된 방송이 없습니다.")
                } else {
                    state.todaySchedules.value.forEach { ScheduleCard(it) }
                }
            }
        }

        SectionHeader("추천 인플루언서")
        SectionStateSummary(state.featuredInfluencers)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.featuredInfluencers.value.forEach { item ->
                InfluencerCard(
                    item = item,
                    onClick = { onOpenInfluencer(item.slug) },
                    onToggleFavorite = { onToggleFavorite(item.slug) },
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Partial failure는 섹션 단위로 표현됩니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
