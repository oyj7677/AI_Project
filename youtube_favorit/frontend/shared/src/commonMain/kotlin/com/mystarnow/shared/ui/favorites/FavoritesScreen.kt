package com.mystarnow.shared.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mystarnow.shared.presentation.favorites.FavoritesUiState
import com.mystarnow.shared.ui.components.EmptyStateCard
import com.mystarnow.shared.ui.components.InfluencerCard
import com.mystarnow.shared.ui.components.SectionHeader

@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onOpenInfluencer: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading && state.items.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
            Text("즐겨찾기 불러오는 중")
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader("즐겨찾기")
        if (state.isLoading) {
            Text(
                "즐겨찾기를 새로고침하는 중입니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.items.isEmpty()) {
            EmptyStateCard("아직 비어 있어요", "인플루언서를 즐겨찾기에 추가해 보세요.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.items) { item ->
                    InfluencerCard(
                        item = item,
                        onClick = { onOpenInfluencer(item.slug) },
                        onToggleFavorite = { onToggleFavorite(item.slug) },
                    )
                }
            }
        }
        Text(
            "즐겨찾기는 MVP에서 기기 로컬에만 저장됩니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
