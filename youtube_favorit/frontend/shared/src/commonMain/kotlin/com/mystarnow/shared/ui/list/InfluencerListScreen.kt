package com.mystarnow.shared.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mystarnow.shared.domain.model.Platform
import com.mystarnow.shared.presentation.list.InfluencerListUiState
import com.mystarnow.shared.ui.components.EmptyStateCard
import com.mystarnow.shared.ui.components.InfluencerCard
import com.mystarnow.shared.ui.components.RetryCard
import com.mystarnow.shared.ui.components.SectionHeader
import com.mystarnow.shared.ui.components.SectionStateSummary

@Composable
fun InfluencerListScreen(
    state: InfluencerListUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelectCategory: (String?) -> Unit,
    onTogglePlatform: (Platform) -> Unit,
    onSortChange: (String) -> Unit,
    onOpenInfluencer: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading && state.results.value.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
            Text("인플루언서 목록을 불러오는 중입니다.")
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader("인플루언서 탐색")
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("이름 또는 소개 검색") },
            singleLine = true,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onSearch) { Text("검색") }
            state.filters.value.sortOptions.forEach { option ->
                AssistChip(
                    onClick = { onSortChange(option.value) },
                    label = { Text(if (state.sort == option.value) "✓ ${option.label}" else option.label) },
                )
            }
        }

        Text("카테고리", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.filters.value.categories.forEach { category ->
                AssistChip(
                    onClick = { onSelectCategory(category.value) },
                    label = {
                        Text(
                            if (state.selectedCategory == category.value) "✓ ${category.label}" else category.label
                        )
                    },
                )
            }
        }

        Text("플랫폼", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.filters.value.platforms.forEach { option ->
                val platform = Platform.fromApi(option.value)
                AssistChip(
                    onClick = { if (option.enabled) onTogglePlatform(platform) },
                    label = {
                        Text(
                            if (platform in state.selectedPlatforms) "✓ ${option.label}" else option.label
                        )
                    },
                )
            }
        }

        SectionStateSummary(state.results)
        if (state.results.value.isEmpty()) {
            if (state.results.status == com.mystarnow.shared.core.model.SectionStatus.FAILED) {
                RetryCard("목록 로딩 실패", state.results.message ?: "다시 시도해 주세요.", onRetry = onSearch)
            } else {
                EmptyStateCard("결과 없음", "조건에 맞는 인플루언서를 찾지 못했습니다.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.results.value) { item ->
                    InfluencerCard(
                        item = item,
                        onClick = { onOpenInfluencer(item.slug) },
                        onToggleFavorite = { onToggleFavorite(item.slug) },
                    )
                }
                if (state.hasNext) {
                    item {
                        OutlinedButton(onClick = onLoadMore, modifier = Modifier.fillMaxWidth()) {
                            Text("더 보기")
                        }
                    }
                }
            }
        }
    }
}
