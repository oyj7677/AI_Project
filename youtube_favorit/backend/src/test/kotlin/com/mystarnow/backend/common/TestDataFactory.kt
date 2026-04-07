package com.mystarnow.backend.common

import com.mystarnow.backend.persistence.entity.ActivityItemEntity
import com.mystarnow.backend.persistence.entity.ChannelEntity
import com.mystarnow.backend.persistence.entity.InfluencerCategoryEntity
import com.mystarnow.backend.persistence.entity.InfluencerCategoryId
import com.mystarnow.backend.persistence.entity.InfluencerEntity
import com.mystarnow.backend.persistence.entity.LiveStatusCacheEntity
import com.mystarnow.backend.persistence.entity.PlatformSyncMetadataEntity
import com.mystarnow.backend.persistence.entity.ScheduleItemEntity
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.util.UUID

object TestDataFactory {
    fun namedUuid(seed: String): UUID =
        UUID.nameUUIDFromBytes(seed.toByteArray(StandardCharsets.UTF_8))

    fun influencer(
        slug: String,
        displayName: String,
        featured: Boolean = false,
        liveNow: Boolean = false,
    ): InfluencerEntity = InfluencerEntity(
        id = namedUuid("influencer-$slug"),
        slug = slug,
        displayName = displayName,
        normalizedName = slug.replace("-", ""),
        bio = "$displayName bio",
        profileImageUrl = "https://cdn.test/$slug.jpg",
        status = "active",
        featured = featured,
        defaultTimezone = "Asia/Seoul",
        latestActivityAt = OffsetDateTime.parse("2026-04-04T11:00:00Z"),
        currentLivePlatform = if (liveNow) "youtube" else null,
        liveNow = liveNow,
    )

    fun categoryLink(influencerId: UUID, code: String): InfluencerCategoryEntity =
        InfluencerCategoryEntity(
            id = InfluencerCategoryId(influencerId = influencerId, categoryCode = code),
        )

    fun youtubeChannel(influencer: InfluencerEntity): ChannelEntity =
        ChannelEntity(
            id = namedUuid("channel-${influencer.slug}-youtube"),
            influencerId = influencer.id,
            platformCode = "youtube",
            externalChannelId = "yt-${influencer.slug}",
            handle = "@${influencer.slug}",
            channelUrl = "https://youtube.com/@${influencer.slug}",
            displayLabel = "YouTube",
            official = true,
            primary = true,
            status = "active",
        )

    fun instagramChannel(influencer: InfluencerEntity): ChannelEntity =
        ChannelEntity(
            id = namedUuid("channel-${influencer.slug}-instagram"),
            influencerId = influencer.id,
            platformCode = "instagram",
            externalChannelId = "ig-${influencer.slug}",
            handle = "@${influencer.slug}.daily",
            channelUrl = "https://instagram.com/${influencer.slug}.daily",
            displayLabel = "Instagram",
            official = true,
            primary = false,
            status = "active",
        )

    fun activity(
        influencerId: UUID,
        channelId: UUID,
        platform: String,
        sourceType: String,
        title: String,
        publishedAt: OffsetDateTime,
    ): ActivityItemEntity = ActivityItemEntity(
        id = UUID.randomUUID(),
        influencerId = influencerId,
        channelId = channelId,
        platformCode = platform,
        sourceActivityId = "$platform-$title",
        sourceType = sourceType,
        contentType = if (platform == "instagram") "post" else "video",
        title = title,
        summary = "$title summary",
        thumbnailUrl = null,
        publishedAt = publishedAt,
        externalUrl = "https://example.com/$title",
        pinned = false,
        freshnessStatus = if (sourceType == "manual") "manual" else "fresh",
    )

    fun liveStatus(
        channelId: UUID,
        influencerId: UUID,
        isLive: Boolean,
    ): LiveStatusCacheEntity = LiveStatusCacheEntity(
        channelId = channelId,
        influencerId = influencerId,
        platformCode = "youtube",
        live = isLive,
        liveTitle = if (isLive) "Live stream" else null,
        watchUrl = if (isLive) "https://youtube.com/watch?v=live" else null,
        viewerCount = if (isLive) 100 else null,
        startedAt = if (isLive) OffsetDateTime.parse("2026-04-04T11:40:00Z") else null,
        snapshotAt = OffsetDateTime.parse("2026-04-04T11:59:00Z"),
        staleAt = OffsetDateTime.parse("2026-04-04T12:04:00Z"),
        freshnessStatus = "fresh",
        lastSuccessfulSyncAt = OffsetDateTime.parse("2026-04-04T11:59:00Z"),
        lastAttemptedSyncAt = OffsetDateTime.parse("2026-04-04T11:59:00Z"),
    )

    fun schedule(
        influencerId: UUID,
        channelId: UUID?,
        title: String,
        scheduledAt: OffsetDateTime,
    ): ScheduleItemEntity = ScheduleItemEntity(
        id = UUID.randomUUID(),
        influencerId = influencerId,
        channelId = channelId,
        platformCode = "youtube",
        sourceType = "manual",
        status = "scheduled",
        title = title,
        note = "note",
        scheduledAt = scheduledAt,
        createdByOperator = "test",
        updatedByOperator = "test",
    )

    fun syncMetadata(
        channelId: UUID,
        influencerId: UUID,
        scope: String,
        status: String,
        consecutiveFailures: Int = 0,
        errorCode: String? = null,
        errorMessage: String? = null,
        nextScheduledAt: OffsetDateTime? = OffsetDateTime.parse("2026-04-04T12:03:00Z"),
        backoffUntil: OffsetDateTime? = null,
    ): PlatformSyncMetadataEntity = PlatformSyncMetadataEntity(
        id = UUID.randomUUID(),
        platformCode = "youtube",
        resourceScope = scope,
        channelId = channelId,
        influencerId = influencerId,
        syncKey = "youtube:channel:$channelId:$scope",
        lastAttemptedAt = OffsetDateTime.parse("2026-04-04T11:59:00Z"),
        lastSucceededAt = if (status == "success") OffsetDateTime.parse("2026-04-04T11:59:00Z") else OffsetDateTime.parse("2026-04-04T11:40:00Z"),
        lastStatus = status,
        lastErrorCode = errorCode,
        lastErrorMessage = errorMessage,
        consecutiveFailures = consecutiveFailures,
        nextScheduledAt = nextScheduledAt,
        backoffUntil = backoffUntil,
    )
}
