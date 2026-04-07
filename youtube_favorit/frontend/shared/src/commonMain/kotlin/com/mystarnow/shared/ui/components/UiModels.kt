package com.mystarnow.shared.ui.components

import com.mystarnow.shared.core.model.AppMode
import com.mystarnow.shared.core.model.DeveloperSettings
import com.mystarnow.shared.core.model.Freshness
import com.mystarnow.shared.core.model.SectionModel
import com.mystarnow.shared.core.model.SectionStatus
import com.mystarnow.shared.domain.model.ActivityItem
import com.mystarnow.shared.domain.model.Channel
import com.mystarnow.shared.domain.model.FeatureFlags
import com.mystarnow.shared.domain.model.InfluencerProfile
import com.mystarnow.shared.domain.model.InfluencerSummary
import com.mystarnow.shared.domain.model.LiveNowCard
import com.mystarnow.shared.domain.model.LiveStatus
import com.mystarnow.shared.domain.model.Platform
import com.mystarnow.shared.domain.model.ScheduleItem

data class UiSectionState<T>(
    val status: SectionStatus,
    val freshness: Freshness,
    val generatedAt: String? = null,
    val staleAt: String? = null,
    val value: T,
    val message: String? = null,
    val retryable: Boolean = false,
    val source: String? = null,
) {
    companion object {
        fun <T> empty(value: T): UiSectionState<T> = UiSectionState(
            status = SectionStatus.EMPTY,
            freshness = Freshness.UNKNOWN,
            generatedAt = null,
            staleAt = null,
            value = value,
            message = null,
            retryable = false,
            source = null,
        )

        fun <T> failed(value: T, message: String): UiSectionState<T> = UiSectionState(
            status = SectionStatus.FAILED,
            freshness = Freshness.UNKNOWN,
            generatedAt = null,
            staleAt = null,
            value = value,
            message = message,
            retryable = true,
            source = null,
        )
    }
}

data class InfluencerCardUiModel(
    val slug: String,
    val name: String,
    val summary: String,
    val categories: String,
    val platforms: String,
    val liveBadge: String,
    val recentActivityAt: String?,
    val isFavorite: Boolean,
)

data class LiveNowUiModel(
    val slug: String,
    val name: String,
    val platformLabel: String,
    val liveTitle: String,
    val startedAt: String?,
    val watchUrl: String?,
)

data class ActivityCardUiModel(
    val title: String,
    val subtitle: String,
    val platformLabel: String,
    val externalUrl: String,
)

data class ScheduleRowUiModel(
    val title: String,
    val scheduleText: String,
    val platformLabel: String,
    val note: String?,
)

data class ChannelUiModel(
    val label: String,
    val handle: String?,
    val url: String,
    val isPrimary: Boolean,
)

data class ProfileUiModel(
    val slug: String,
    val name: String,
    val bio: String,
    val categories: String,
    val isFavorite: Boolean,
)

data class ChipOptionUiModel(
    val value: String,
    val label: String,
    val enabled: Boolean = true,
)

data class FilterBarUiModel(
    val categories: List<ChipOptionUiModel> = emptyList(),
    val platforms: List<ChipOptionUiModel> = emptyList(),
    val sortOptions: List<ChipOptionUiModel> = emptyList(),
)

data class AppFeatureFlagsUiModel(
    val showSchedules: Boolean = true,
    val showRecentActivities: Boolean = true,
    val enableLiveNow: Boolean = true,
)

data class DeveloperSettingsUiModel(
    val mode: AppMode = AppMode.MOCK,
    val baseUrl: String = "http://10.0.2.2:8080",
)

data class SectionDebugUiModel(
    val requestId: String,
    val generatedAt: String,
    val partialFailure: Boolean,
    val summary: String,
)

fun InfluencerSummary.toUiModel(isFavorite: Boolean): InfluencerCardUiModel = InfluencerCardUiModel(
    slug = slug,
    name = name,
    summary = summary,
    categories = categories.joinToString(" · "),
    platforms = platforms.joinToString(" · ") { it.toDisplayLabel() },
    liveBadge = liveStatus.uppercase(),
    recentActivityAt = recentActivityAt?.toReadableTimestamp(),
    isFavorite = isFavorite,
)

fun LiveNowCard.toUiModel(): LiveNowUiModel = LiveNowUiModel(
    slug = slug,
    name = name,
    platformLabel = platform.toDisplayLabel(),
    liveTitle = liveTitle,
    startedAt = startedAt?.toReadableTimestamp(),
    watchUrl = watchUrl,
)

fun ActivityItem.toUiModel(): ActivityCardUiModel = ActivityCardUiModel(
    title = title,
    subtitle = "${platform.toDisplayLabel()} · ${publishedAt.toReadableTimestamp()}",
    platformLabel = platform.toDisplayLabel(),
    externalUrl = externalUrl,
)

fun ScheduleItem.toUiModel(): ScheduleRowUiModel = ScheduleRowUiModel(
    title = title,
    scheduleText = scheduledAt.toReadableTimestamp(),
    platformLabel = platform?.toDisplayLabel() ?: "TBD",
    note = note,
)

fun Channel.toUiModel(): ChannelUiModel = ChannelUiModel(
    label = platform.toDisplayLabel(),
    handle = handle,
    url = channelUrl,
    isPrimary = isPrimary,
)

fun InfluencerProfile.toUiModel(isFavorite: Boolean): ProfileUiModel = ProfileUiModel(
    slug = slug,
    name = name,
    bio = bio.orEmpty(),
    categories = categories.joinToString(" · "),
    isFavorite = isFavorite,
)

fun LiveStatus.toStatusText(): String = when {
    isLive && platform != null -> "LIVE NOW · ${platform.toDisplayLabel()}"
    !isLive -> "OFFLINE"
    else -> "STATUS UNKNOWN"
}

fun FeatureFlags.toUiModel(): AppFeatureFlagsUiModel = AppFeatureFlagsUiModel(
    showSchedules = showSchedules,
    showRecentActivities = showRecentActivities,
    enableLiveNow = enableLiveNow,
)

fun DeveloperSettings.toUiModel(): DeveloperSettingsUiModel = DeveloperSettingsUiModel(
    mode = mode,
    baseUrl = baseUrl,
)

fun <T, R> SectionModel<T>.toUiState(transform: (T) -> R): UiSectionState<R> = UiSectionState(
    status = status,
    freshness = freshness,
    generatedAt = generatedAt,
    staleAt = staleAt,
    value = transform(data),
    message = error?.message,
    retryable = error?.retryable == true,
    source = error?.source,
)

private fun String.toReadableTimestamp(): String = removeSuffix("Z").replace("T", " ")

private fun Platform.toDisplayLabel(): String = when (this) {
    Platform.YOUTUBE -> "YouTube"
    Platform.INSTAGRAM -> "Instagram"
    Platform.X -> "X"
    Platform.CHZZK -> "CHZZK"
    Platform.SOOP -> "SOOP"
}

fun UiSectionState<*>.statusLabel(): String = when (status) {
    SectionStatus.SUCCESS -> "성공"
    SectionStatus.PARTIAL -> "부분 실패"
    SectionStatus.FAILED -> "실패"
    SectionStatus.EMPTY -> "빈 상태"
}

fun UiSectionState<*>.freshnessLabel(): String = when (freshness) {
    Freshness.FRESH -> "fresh"
    Freshness.STALE -> "stale"
    Freshness.MANUAL -> "manual"
    Freshness.UNKNOWN -> "unknown"
}
