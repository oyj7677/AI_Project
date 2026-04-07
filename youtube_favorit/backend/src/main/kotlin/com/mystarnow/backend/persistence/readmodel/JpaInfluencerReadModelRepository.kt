package com.mystarnow.backend.persistence.readmodel

import com.mystarnow.backend.persistence.repository.ActivityItemRepository
import com.mystarnow.backend.persistence.repository.CategoryRepository
import com.mystarnow.backend.persistence.repository.ChannelRepository
import com.mystarnow.backend.persistence.repository.ChannelOperatorMetadataRepository
import com.mystarnow.backend.persistence.repository.InfluencerCategoryRepository
import com.mystarnow.backend.persistence.repository.InfluencerOperatorMetadataRepository
import com.mystarnow.backend.persistence.repository.InfluencerRepository
import com.mystarnow.backend.persistence.repository.InfluencerServingStateRepository
import com.mystarnow.backend.persistence.repository.LiveStatusCacheRepository
import com.mystarnow.backend.persistence.repository.ScheduleItemRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class JpaInfluencerReadModelRepository(
    private val influencerRepository: InfluencerRepository,
    private val influencerCategoryRepository: InfluencerCategoryRepository,
    private val categoryRepository: CategoryRepository,
    private val channelRepository: ChannelRepository,
    private val channelOperatorMetadataRepository: ChannelOperatorMetadataRepository,
    private val influencerOperatorMetadataRepository: InfluencerOperatorMetadataRepository,
    private val servingStateRepository: InfluencerServingStateRepository,
    private val liveStatusCacheRepository: LiveStatusCacheRepository,
    private val activityItemRepository: ActivityItemRepository,
    private val scheduleItemRepository: ScheduleItemRepository,
) : InfluencerReadModelRepository {
    private val objectMapper = jacksonObjectMapper()

    override fun loadActiveInfluencers(): List<InfluencerAggregate> {
        val influencers = influencerRepository.findAllByStatusAndDeletedAtIsNull("active")
        return assembleInfluencers(influencers.map { it.id }, influencers)
            .filter { it.servingState?.homeVisibility != false }
    }

    override fun loadInfluencerBySlug(slug: String): InfluencerAggregate? {
        val influencer = influencerRepository.findBySlugAndStatusAndDeletedAtIsNull(slug, "active") ?: return null
        return assembleInfluencers(listOf(influencer.id), listOf(influencer)).firstOrNull()
            ?.takeIf { it.servingState?.detailVisibility != false }
    }

    override fun loadLatestActivities(limit: Int): List<ActivityRecord> =
        activityItemRepository.findTop50ByOrderByPublishedAtDesc()
            .take(limit)
            .map {
                ActivityRecord(
                    id = it.id,
                    influencerId = it.influencerId,
                    platform = it.platformCode,
                    contentType = it.contentType,
                    title = it.title,
                    summary = it.summary,
                    thumbnailUrl = it.thumbnailUrl,
                    publishedAt = it.publishedAt,
                    externalUrl = it.externalUrl,
                    freshnessStatus = it.freshnessStatus,
                    staleAt = it.staleAt,
                )
            }

    override fun loadSchedulesBetween(start: OffsetDateTime, end: OffsetDateTime): List<ScheduleRecord> =
        scheduleItemRepository.findAllByScheduledAtBetweenAndStatusOrderByScheduledAtAsc(start, end, "scheduled")
            .map {
                ScheduleRecord(
                    id = it.id,
                    influencerId = it.influencerId,
                    platform = it.platformCode,
                    title = it.title,
                    note = it.note,
                    scheduledAt = it.scheduledAt,
                )
            }

    private fun assembleInfluencers(
        influencerIds: List<UUID>,
        influencers: List<com.mystarnow.backend.persistence.entity.InfluencerEntity>,
    ): List<InfluencerAggregate> {
        if (influencerIds.isEmpty()) {
            return emptyList()
        }

        val categoryLinks = influencerCategoryRepository.findAllByIdInfluencerIdIn(influencerIds)
        val categoryCodes = categoryLinks.map { it.id.categoryCode }.distinct()
        val categoryMap = categoryRepository.findAllById(categoryCodes)
            .associateBy { it.categoryCode }
        val categoriesByInfluencer = categoryLinks.groupBy { it.id.influencerId }

        val channels = channelRepository.findAllByInfluencerIdInAndDeletedAtIsNull(influencerIds)
        val channelsByInfluencer = channels.groupBy { it.influencerId }
        val channelOverrides = channelOperatorMetadataRepository.findAllByChannelIdIn(channels.map { it.id })
            .associateBy { it.channelId }
        val influencerOverrides = influencerOperatorMetadataRepository.findAllByInfluencerIdIn(influencerIds)
            .associateBy { it.influencerId }
        val servingStateMap = servingStateRepository.findAllByInfluencerIdIn(influencerIds)
            .associateBy { it.influencerId }
        val liveStatusesByChannel = liveStatusCacheRepository.findAllByChannelIdIn(channels.map { it.id })
            .associateBy { it.channelId }
        val activitiesByInfluencer = activityItemRepository.findAllByInfluencerIdInOrderByPublishedAtDesc(influencerIds)
            .groupBy { it.influencerId }
        val now = OffsetDateTime.now()
        val schedulesByInfluencer = influencerIds.associateWith { influencerId ->
            scheduleItemRepository.findAllByInfluencerIdAndScheduledAtGreaterThanEqualOrderByScheduledAtAsc(influencerId, now)
        }

        return influencers.map { influencer ->
            val influencerOverride = influencerOverrides[influencer.id]
            val servingStateEntity = servingStateMap[influencer.id]
            val linkedCategories = categoriesByInfluencer[influencer.id].orEmpty()
                .mapNotNull { categoryMap[it.id.categoryCode] }
                .sortedBy { it.sortOrder }
                .map { CategoryRecord(it.categoryCode, it.displayName) }
            val influencerChannels = channelsByInfluencer[influencer.id].orEmpty()
                .map {
                    val channelOverride = channelOverrides[it.id]
                    ChannelRecord(
                        id = it.id,
                        platform = it.platformCode,
                        handle = channelOverride?.overrideHandle ?: it.handle,
                        channelUrl = channelOverride?.overrideChannelUrl ?: it.channelUrl,
                        isOfficial = channelOverride?.overrideIsOfficial ?: it.official,
                        isPrimary = channelOverride?.overrideIsPrimary ?: it.primary,
                        externalChannelId = it.externalChannelId,
                    )
                }
            val influencerLiveStatuses = channelsByInfluencer[influencer.id].orEmpty()
                .mapNotNull { channel -> liveStatusesByChannel[channel.id] }
                .map {
                    LiveStatusRecord(
                        channelId = it.channelId,
                        influencerId = it.influencerId,
                        platform = it.platformCode,
                        isLive = it.live,
                        liveTitle = it.liveTitle,
                        watchUrl = it.watchUrl,
                        viewerCount = it.viewerCount,
                        startedAt = it.startedAt,
                        snapshotAt = it.snapshotAt,
                        freshnessStatus = it.freshnessStatus,
                        staleAt = it.staleAt,
                    )
                }
                .sortedByDescending { it.snapshotAt }
            val influencerActivities = activitiesByInfluencer[influencer.id].orEmpty()
                .map {
                    ActivityRecord(
                        id = it.id,
                        influencerId = it.influencerId,
                        platform = it.platformCode,
                        contentType = it.contentType,
                        title = it.title,
                        summary = it.summary,
                        thumbnailUrl = it.thumbnailUrl,
                        publishedAt = it.publishedAt,
                        externalUrl = it.externalUrl,
                        freshnessStatus = it.freshnessStatus,
                        staleAt = it.staleAt,
                    )
                }
            val influencerSchedules = schedulesByInfluencer[influencer.id].orEmpty()
                .map {
                    ScheduleRecord(
                        id = it.id,
                        influencerId = it.influencerId,
                        platform = it.platformCode,
                        title = it.title,
                        note = it.note,
                        scheduledAt = it.scheduledAt,
                    )
                }

            val servingState = servingStateEntity?.let {
                ServingStateRecord(
                    influencerId = it.influencerId,
                    liveNow = it.liveNow,
                    livePlatformCode = it.livePlatformCode,
                    liveStartedAt = it.liveStartedAt,
                    latestActivityAt = it.latestActivityAt,
                    latestScheduleAt = it.latestScheduleAt,
                    supportedPlatforms = objectMapper.readValue(it.supportedPlatformsCache),
                    featuredRank = it.featuredRank,
                    homeVisibility = it.homeVisibility,
                    detailVisibility = it.detailVisibility,
                    lastProjectionRefreshAt = it.lastProjectionRefreshAt,
                )
            }

            InfluencerAggregate(
                id = influencer.id,
                slug = influencer.slug,
                displayName = influencerOverride?.overrideDisplayName ?: influencer.displayName,
                bio = influencerOverride?.overrideBio ?: influencer.bio,
                profileImageUrl = influencerOverride?.overrideProfileImageUrl ?: influencer.profileImageUrl,
                featured = influencer.featured,
                latestActivityAt = servingState?.latestActivityAt ?: influencer.latestActivityAt,
                currentLivePlatform = servingState?.livePlatformCode ?: influencer.currentLivePlatform,
                liveNow = servingState?.liveNow ?: influencer.liveNow,
                categories = linkedCategories,
                channels = influencerChannels,
                liveStatuses = influencerLiveStatuses,
                recentActivities = influencerActivities,
                schedules = influencerSchedules,
                servingState = servingState,
            )
        }
    }
}
