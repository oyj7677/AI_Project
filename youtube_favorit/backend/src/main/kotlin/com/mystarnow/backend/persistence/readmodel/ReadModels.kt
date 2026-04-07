package com.mystarnow.backend.persistence.readmodel

import java.time.OffsetDateTime
import java.util.UUID

data class CategoryRecord(
    val code: String,
    val displayName: String,
)

data class ChannelRecord(
    val id: UUID,
    val platform: String,
    val handle: String?,
    val channelUrl: String,
    val isOfficial: Boolean,
    val isPrimary: Boolean,
    val externalChannelId: String,
)

data class ActivityRecord(
    val id: UUID,
    val influencerId: UUID,
    val platform: String,
    val contentType: String,
    val title: String,
    val summary: String?,
    val thumbnailUrl: String?,
    val publishedAt: OffsetDateTime,
    val externalUrl: String,
    val freshnessStatus: String,
    val staleAt: OffsetDateTime?,
)

data class LiveStatusRecord(
    val channelId: UUID,
    val influencerId: UUID,
    val platform: String,
    val isLive: Boolean,
    val liveTitle: String?,
    val watchUrl: String?,
    val viewerCount: Int?,
    val startedAt: OffsetDateTime?,
    val snapshotAt: OffsetDateTime,
    val freshnessStatus: String,
    val staleAt: OffsetDateTime?,
)

data class ScheduleRecord(
    val id: UUID,
    val influencerId: UUID,
    val platform: String?,
    val title: String,
    val note: String?,
    val scheduledAt: OffsetDateTime,
)

data class ServingStateRecord(
    val influencerId: UUID,
    val liveNow: Boolean,
    val livePlatformCode: String?,
    val liveStartedAt: OffsetDateTime?,
    val latestActivityAt: OffsetDateTime?,
    val latestScheduleAt: OffsetDateTime?,
    val supportedPlatforms: List<String>,
    val featuredRank: Int?,
    val homeVisibility: Boolean,
    val detailVisibility: Boolean,
    val lastProjectionRefreshAt: OffsetDateTime,
)

data class InfluencerAggregate(
    val id: UUID,
    val slug: String,
    val displayName: String,
    val bio: String?,
    val profileImageUrl: String?,
    val featured: Boolean,
    val latestActivityAt: OffsetDateTime?,
    val currentLivePlatform: String?,
    val liveNow: Boolean,
    val categories: List<CategoryRecord>,
    val channels: List<ChannelRecord>,
    val liveStatuses: List<LiveStatusRecord>,
    val recentActivities: List<ActivityRecord>,
    val schedules: List<ScheduleRecord>,
    val servingState: ServingStateRecord?,
)

interface InfluencerReadModelRepository {
    fun loadActiveInfluencers(): List<InfluencerAggregate>

    fun loadInfluencerBySlug(slug: String): InfluencerAggregate?

    fun loadLatestActivities(limit: Int): List<ActivityRecord>

    fun loadSchedulesBetween(start: OffsetDateTime, end: OffsetDateTime): List<ScheduleRecord>
}
