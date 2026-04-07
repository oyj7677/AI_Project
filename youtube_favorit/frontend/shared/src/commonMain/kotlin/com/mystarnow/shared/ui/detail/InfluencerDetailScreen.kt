package com.mystarnow.shared.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.mystarnow.shared.presentation.detail.InfluencerDetailUiState
import com.mystarnow.shared.ui.components.ActivityCard
import com.mystarnow.shared.ui.components.ChannelCard
import com.mystarnow.shared.ui.components.EmptyStateCard
import com.mystarnow.shared.ui.components.SectionStateSummary
import com.mystarnow.shared.ui.components.ScheduleCard
import com.mystarnow.shared.ui.components.SectionHeader

@Composable
fun InfluencerDetailScreen(
    state: InfluencerDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenExternalLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading && state.profile.value == null) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
            Text("상세 정보를 불러오는 중입니다.")
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack) { Text("뒤로") }
            OutlinedButton(onClick = onRetry) { Text("새로고침") }
            OutlinedButton(onClick = onToggleFavorite) {
                Text(if (state.profile.value?.isFavorite == true) "즐겨찾기 해제" else "즐겨찾기 추가")
            }
        }

        state.profile.value?.let { profile ->
            SectionHeader(profile.name, profile.bio)
            SectionStateSummary(state.profile)
            Text(profile.categories, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } ?: EmptyStateCard("프로필 없음", state.profile.message ?: "상세 프로필을 불러오지 못했습니다.")

        state.liveStatus.value?.let { live ->
            SectionHeader("현재 상태", live.statusText)
            SectionStateSummary(state.liveStatus)
            live.liveTitle?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
            live.startedAt?.let { Text("시작: $it", style = MaterialTheme.typography.bodySmall) }
            live.watchUrl?.let { url ->
                OutlinedButton(onClick = { onOpenExternalLink(url) }) {
                    Text("시청하기")
                }
            }
        }

        SectionHeader("공식 채널")
        SectionStateSummary(state.channels)
        if (state.channels.value.isEmpty()) {
            EmptyStateCard("채널 없음", "등록된 공식 채널이 없습니다.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.channels.value.forEach { item ->
                    ChannelCard(item = item, onOpen = { onOpenExternalLink(item.url) })
                }
            }
        }

        SectionHeader("최근 활동", state.recentActivities.message)
        SectionStateSummary(state.recentActivities)
        if (state.recentActivities.value.isEmpty()) {
            EmptyStateCard("최근 활동 없음", "최근 활동을 아직 확인하지 못했습니다.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.recentActivities.value.forEach { item ->
                    ActivityCard(item = item, onOpen = { onOpenExternalLink(item.externalUrl) })
                }
            }
        }

        SectionHeader("예정 방송")
        SectionStateSummary(state.schedules)
        if (state.schedules.value.isEmpty()) {
            EmptyStateCard("예정 방송 없음", "등록된 방송 일정이 없습니다.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.schedules.value.forEach { ScheduleCard(it) }
            }
        }
    }
}
