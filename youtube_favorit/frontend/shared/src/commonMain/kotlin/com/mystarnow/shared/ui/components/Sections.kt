package com.mystarnow.shared.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mystarnow.shared.core.model.AppMode
import com.mystarnow.shared.core.model.SectionStatus

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        subtitle?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SectionStateSummary(section: UiSectionState<*>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(onClick = {}, label = { Text(section.statusLabel()) })
        AssistChip(onClick = {}, label = { Text(section.freshnessLabel()) })
        section.source?.let { source ->
            AssistChip(onClick = {}, label = { Text(source) })
        }
    }
    val timingText = buildString {
        section.generatedAt?.let { append("updated=$it") }
        section.staleAt?.let {
            if (isNotBlank()) append(" · ")
            append("staleAt=$it")
        }
    }
    if (timingText.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = timingText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    section.message?.let {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = it,
            style = MaterialTheme.typography.bodySmall,
            color = if (section.status == SectionStatus.FAILED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun RetryCard(
    title: String,
    description: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onRetry) {
                Text("다시 시도")
            }
        }
    }
}

@Composable
fun DeveloperPanel(
    settings: DeveloperSettingsUiModel,
    featureFlags: AppFeatureFlagsUiModel,
    debugInfo: SectionDebugUiModel?,
    onModeChange: (AppMode) -> Unit,
    onBaseUrlChange: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var draftBaseUrl by remember(settings.baseUrl) { mutableStateOf(settings.baseUrl) }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("개발자 패널", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "접기" else "열기")
                }
            }

            if (expanded) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { onModeChange(AppMode.MOCK) },
                        label = { Text(if (settings.mode == AppMode.MOCK) "MOCK ON" else "MOCK") },
                    )
                    AssistChip(
                        onClick = { onModeChange(AppMode.LIVE) },
                        label = { Text(if (settings.mode == AppMode.LIVE) "LIVE ON" else "LIVE") },
                    )
                }

                OutlinedTextField(
                    value = draftBaseUrl,
                    onValueChange = { draftBaseUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("API Base URL") },
                    singleLine = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onBaseUrlChange(draftBaseUrl) }) {
                        Text("적용")
                    }
                    OutlinedButton(onClick = onRefresh) {
                        Text("강제 새로고침")
                    }
                }

                Text(
                    "Feature Flags: live=${featureFlags.enableLiveNow}, updates=${featureFlags.showRecentActivities}, schedules=${featureFlags.showSchedules}",
                    style = MaterialTheme.typography.bodySmall,
                )
                debugInfo?.let {
                    Text("requestId=${it.requestId}", style = MaterialTheme.typography.bodySmall)
                    Text("generatedAt=${it.generatedAt}", style = MaterialTheme.typography.bodySmall)
                    Text("partialFailure=${it.partialFailure}", style = MaterialTheme.typography.bodySmall)
                    Text(it.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun InfluencerCard(
    item: InfluencerCardUiModel,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (item.isFavorite) "★" else "☆",
                    modifier = Modifier.clickable(onClick = onToggleFavorite),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.summary, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(item.liveBadge) })
                AssistChip(onClick = {}, label = { Text(item.platforms) })
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(item.categories, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            item.recentActivityAt?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text("최근 활동: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ActivityCard(
    item: ActivityCardUiModel,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ChannelCard(
    item: ChannelUiModel,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${item.label}${if (item.isPrimary) " · Main" else ""}", style = MaterialTheme.typography.titleMedium)
            item.handle?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun ScheduleCard(item: ScheduleRowUiModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${item.platformLabel} · ${item.scheduleText}", style = MaterialTheme.typography.bodySmall)
            item.note?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun <T> SectionContent(
    section: UiSectionState<List<T>>,
    emptyTitle: String,
    emptyDescription: String,
    onRetry: () -> Unit,
    itemContent: @Composable (T) -> Unit,
) {
    when (section.status) {
        SectionStatus.FAILED -> RetryCard(
            title = emptyTitle,
            description = section.message ?: emptyDescription,
            onRetry = onRetry,
        )

        SectionStatus.EMPTY -> EmptyStateCard(emptyTitle, emptyDescription)
        else -> {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(section.value) { item ->
                    itemContent(item)
                }
            }
        }
    }
}
