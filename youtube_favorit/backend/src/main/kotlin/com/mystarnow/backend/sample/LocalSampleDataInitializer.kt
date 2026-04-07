package com.mystarnow.backend.sample

import com.mystarnow.backend.common.config.AppProperties
import com.mystarnow.backend.persistence.entity.ActivityItemEntity
import com.mystarnow.backend.persistence.entity.ChannelEntity
import com.mystarnow.backend.persistence.entity.InfluencerCategoryEntity
import com.mystarnow.backend.persistence.entity.InfluencerCategoryId
import com.mystarnow.backend.persistence.entity.InfluencerEntity
import com.mystarnow.backend.persistence.entity.LiveStatusCacheEntity
import com.mystarnow.backend.persistence.entity.ScheduleItemEntity
import com.mystarnow.backend.persistence.entity.PlatformSyncMetadataEntity
import com.mystarnow.backend.persistence.readmodel.InfluencerServingStateProjector
import com.mystarnow.backend.persistence.repository.ActivityItemRepository
import com.mystarnow.backend.persistence.repository.ChannelRepository
import com.mystarnow.backend.persistence.repository.InfluencerCategoryRepository
import com.mystarnow.backend.persistence.repository.InfluencerRepository
import com.mystarnow.backend.persistence.repository.ScheduleItemRepository
import com.mystarnow.backend.persistence.repository.LiveStatusCacheRepository
import com.mystarnow.backend.persistence.repository.PlatformSyncMetadataRepository
import com.mystarnow.backend.platform.integration.SyncScopes
import com.mystarnow.backend.platform.sync.SyncBackoffPolicy
import com.mystarnow.backend.platform.sync.SyncStatuses
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Configuration
class LocalSampleDataInitializer {
    @Bean
    @Profile("local", "dev", "integration")
    fun sampleDataRunner(
        appProperties: AppProperties,
        influencerRepository: InfluencerRepository,
        influencerCategoryRepository: InfluencerCategoryRepository,
        channelRepository: ChannelRepository,
        activityItemRepository: ActivityItemRepository,
        scheduleItemRepository: ScheduleItemRepository,
        liveStatusCacheRepository: LiveStatusCacheRepository,
        platformSyncMetadataRepository: PlatformSyncMetadataRepository,
        projector: InfluencerServingStateProjector,
        syncBackoffPolicy: SyncBackoffPolicy,
        clock: Clock,
    ): CommandLineRunner = CommandLineRunner {
        if (!appProperties.sampleData.enabled || influencerRepository.count() > 0) {
            return@CommandLineRunner
        }

        val now = OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC)
        val influencers = mutableListOf<InfluencerEntity>()
        val categories = mutableListOf<InfluencerCategoryEntity>()
        val channels = mutableListOf<ChannelEntity>()
        val activities = mutableListOf<ActivityItemEntity>()
        val schedules = mutableListOf<ScheduleItemEntity>()
        val liveStatuses = mutableListOf<LiveStatusCacheEntity>()
        val syncMetadata = mutableListOf<PlatformSyncMetadataEntity>()

        val categoryCycle = listOf("game", "talk", "daily", "music")
        repeat(25) { index ->
            val slug = if (index == 0) "haru" else "creator-${index + 1}"
            val influencerId = namedUuid("influencer-$slug")
            val featured = index < 6
            val youtubeChannelId = namedUuid("youtube-$slug")
            val instagramChannelId = namedUuid("instagram-$slug")
            val latestActivityAt = now.minusHours(index.toLong())
            val isLive = index % 5 == 0
            influencers += InfluencerEntity(
                id = influencerId,
                slug = slug,
                displayName = if (index == 0) "하루" else "크리에이터 ${index + 1}",
                normalizedName = slug.replace("-", ""),
                bio = if (index == 0) "게임, 토크, 일상 소통 중심" else "샘플 인플루언서 ${index + 1}",
                profileImageUrl = "https://cdn.mystarnow.dev/profiles/$slug.png",
                status = "active",
                featured = featured,
                defaultTimezone = "Asia/Seoul",
                latestActivityAt = latestActivityAt,
                currentLivePlatform = if (isLive) "youtube" else null,
                liveNow = isLive,
            )
            categories += InfluencerCategoryEntity(
                id = InfluencerCategoryId(
                    influencerId = influencerId,
                    categoryCode = categoryCycle[index % categoryCycle.size],
                ),
            )
            if (index % 2 == 0) {
                categories += InfluencerCategoryEntity(
                    id = InfluencerCategoryId(
                        influencerId = influencerId,
                        categoryCode = "talk",
                    ),
                )
            }

            channels += ChannelEntity(
                id = youtubeChannelId,
                influencerId = influencerId,
                platformCode = "youtube",
                externalChannelId = "yt-$slug",
                handle = "@$slug",
                channelUrl = "https://youtube.com/@$slug",
                displayLabel = "YouTube",
                official = true,
                primary = true,
                status = "active",
            )
            channels += ChannelEntity(
                id = instagramChannelId,
                influencerId = influencerId,
                platformCode = "instagram",
                externalChannelId = "ig-$slug",
                handle = "@$slug.daily",
                channelUrl = "https://instagram.com/$slug.daily",
                displayLabel = "Instagram",
                official = true,
                primary = false,
                status = "active",
            )

            activities += ActivityItemEntity(
                id = namedUuid("activity-youtube-$slug"),
                influencerId = influencerId,
                channelId = youtubeChannelId,
                platformCode = "youtube",
                sourceType = "imported",
                contentType = if (index % 3 == 0) "short" else "video",
                title = if (index == 0) "어제 하이라이트" else "유튜브 업데이트 ${index + 1}",
                summary = "YouTube sample activity for $slug",
                thumbnailUrl = "https://img.youtube.com/vi/$slug/hqdefault.jpg",
                publishedAt = latestActivityAt,
                externalUrl = "https://youtube.com/watch?v=$slug-video",
                freshnessStatus = "fresh",
            )
            activities += ActivityItemEntity(
                id = namedUuid("activity-instagram-$slug"),
                influencerId = influencerId,
                channelId = instagramChannelId,
                platformCode = "instagram",
                sourceType = "manual",
                contentType = if (index % 4 == 0) "reel" else "post",
                title = if (index == 0) "방송 준비 브이로그" else "인스타 소식 ${index + 1}",
                summary = "Instagram manual-first sample activity for $slug",
                thumbnailUrl = "https://cdn.mystarnow.dev/instagram/$slug.jpg",
                publishedAt = latestActivityAt.minusMinutes(30),
                externalUrl = "https://instagram.com/p/$slug-post",
                freshnessStatus = "manual",
            )

            if (isLive) {
                liveStatuses += LiveStatusCacheEntity(
                    channelId = youtubeChannelId,
                    influencerId = influencerId,
                    platformCode = "youtube",
                    live = true,
                    liveTitle = if (index == 0) "오늘은 잡담 방송" else "라이브 방송 ${index + 1}",
                    watchUrl = "https://youtube.com/watch?v=$slug-live",
                    viewerCount = 1000 + index,
                    startedAt = now.minusMinutes((index + 10).toLong()),
                    snapshotAt = now.minusMinutes(1),
                    staleAt = now.plusMinutes(4),
                    freshnessStatus = "fresh",
                    lastSuccessfulSyncAt = now.minusMinutes(1),
                    lastAttemptedSyncAt = now.minusMinutes(1),
                )
            }

            syncMetadata += listOf(
                PlatformSyncMetadataEntity(
                    id = namedUuid("sync-profile-$slug"),
                    platformCode = "youtube",
                    resourceScope = SyncScopes.CHANNEL_PROFILE,
                    channelId = youtubeChannelId,
                    influencerId = influencerId,
                    syncKey = "youtube:channel:yt-$slug:profile",
                    lastAttemptedAt = now.minusMinutes(1),
                    lastSucceededAt = now.minusMinutes(1),
                    lastStatus = SyncStatuses.SUCCESS,
                    consecutiveFailures = 0,
                    nextScheduledAt = syncBackoffPolicy.nextSuccess(SyncScopes.CHANNEL_PROFILE, now.minusMinutes(1)),
                ),
                PlatformSyncMetadataEntity(
                    id = namedUuid("sync-activity-$slug"),
                    platformCode = "youtube",
                    resourceScope = SyncScopes.CHANNEL_ACTIVITY,
                    channelId = youtubeChannelId,
                    influencerId = influencerId,
                    syncKey = "youtube:channel:yt-$slug:activity",
                    lastAttemptedAt = now.minusMinutes(1),
                    lastSucceededAt = now.minusMinutes(1),
                    lastStatus = SyncStatuses.SUCCESS,
                    consecutiveFailures = 0,
                    nextScheduledAt = syncBackoffPolicy.nextSuccess(SyncScopes.CHANNEL_ACTIVITY, now.minusMinutes(1)),
                ),
                PlatformSyncMetadataEntity(
                    id = namedUuid("sync-live-$slug"),
                    platformCode = "youtube",
                    resourceScope = SyncScopes.CHANNEL_LIVE,
                    channelId = youtubeChannelId,
                    influencerId = influencerId,
                    syncKey = "youtube:channel:yt-$slug:live",
                    lastAttemptedAt = now.minusMinutes(1),
                    lastSucceededAt = now.minusMinutes(1),
                    lastStatus = SyncStatuses.SUCCESS,
                    consecutiveFailures = 0,
                    nextScheduledAt = syncBackoffPolicy.nextSuccess(SyncScopes.CHANNEL_LIVE, now.minusMinutes(1)),
                ),
            )

            schedules += ScheduleItemEntity(
                id = namedUuid("schedule-$slug"),
                influencerId = influencerId,
                channelId = youtubeChannelId,
                platformCode = "youtube",
                sourceType = "manual",
                status = "scheduled",
                title = if (index == 0) "주말 개인 방송" else "예정 방송 ${index + 1}",
                note = if (index % 2 == 0) "변동 가능" else null,
                scheduledAt = now.plusDays((index % 5).toLong()).plusHours((index % 4).toLong()),
            )
        }

        influencerRepository.saveAll(influencers)
        influencerCategoryRepository.saveAll(categories.distinctBy { it.id })
        channelRepository.saveAll(channels)
        activityItemRepository.saveAll(activities)
        liveStatusCacheRepository.saveAll(liveStatuses)
        scheduleItemRepository.saveAll(schedules)
        platformSyncMetadataRepository.saveAll(syncMetadata)
        projector.refreshAll()
    }

    private fun namedUuid(seed: String): UUID =
        UUID.nameUUIDFromBytes(seed.toByteArray(StandardCharsets.UTF_8))
}
