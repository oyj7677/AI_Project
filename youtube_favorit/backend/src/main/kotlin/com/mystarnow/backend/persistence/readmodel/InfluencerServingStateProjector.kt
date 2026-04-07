package com.mystarnow.backend.persistence.readmodel

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mystarnow.backend.persistence.entity.InfluencerServingStateEntity
import com.mystarnow.backend.persistence.repository.ActivityItemRepository
import com.mystarnow.backend.persistence.repository.ChannelRepository
import com.mystarnow.backend.persistence.repository.InfluencerOperatorMetadataRepository
import com.mystarnow.backend.persistence.repository.InfluencerRepository
import com.mystarnow.backend.persistence.repository.InfluencerServingStateRepository
import com.mystarnow.backend.persistence.repository.LiveStatusCacheRepository
import com.mystarnow.backend.persistence.repository.ScheduleItemRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

@Component
class InfluencerServingStateProjector(
    private val influencerRepository: InfluencerRepository,
    private val channelRepository: ChannelRepository,
    private val liveStatusCacheRepository: LiveStatusCacheRepository,
    private val activityItemRepository: ActivityItemRepository,
    private val scheduleItemRepository: ScheduleItemRepository,
    private val operatorMetadataRepository: InfluencerOperatorMetadataRepository,
    private val servingStateRepository: InfluencerServingStateRepository,
    private val clock: Clock,
) {
    private val objectMapper = jacksonObjectMapper()

    @Transactional
    fun refreshInfluencer(influencerId: UUID) {
        val influencer = influencerRepository.findById(influencerId).orElse(null) ?: return
        val channels = channelRepository.findAllByInfluencerIdAndDeletedAtIsNull(influencerId)
        val liveStatuses = liveStatusCacheRepository.findAllByChannelIdIn(channels.map { it.id })
        val activities = activityItemRepository.findAllByInfluencerIdOrderByPublishedAtDesc(influencerId)
        val schedules = scheduleItemRepository.findAllByInfluencerIdAndScheduledAtGreaterThanEqualOrderByScheduledAtAsc(
            influencerId = influencerId,
            start = OffsetDateTime.now(clock).minusMinutes(1),
        )
        val operator = operatorMetadataRepository.findById(influencerId).orElse(null)

        val activeLive = liveStatuses.filter { it.live }.maxByOrNull { it.snapshotAt }
        val supportedPlatforms = channels.map { it.platformCode }.distinct().sorted()
        val latestActivityAt = activities.maxOfOrNull { it.publishedAt }
        val latestScheduleAt = schedules.minOfOrNull { it.scheduledAt }

        val entity = servingStateRepository.findById(influencerId).orElse(
            InfluencerServingStateEntity(
                influencerId = influencerId,
                lastProjectionRefreshAt = OffsetDateTime.now(clock),
            ),
        )
        entity.liveNow = activeLive != null
        entity.livePlatformCode = activeLive?.platformCode
        entity.liveStartedAt = activeLive?.startedAt
        entity.latestActivityAt = latestActivityAt
        entity.latestScheduleAt = latestScheduleAt
        entity.supportedPlatformsCache = objectMapper.writeValueAsString(supportedPlatforms)
        entity.featuredRank = if (influencer.featured) 100 else null
        entity.homeVisibility = operator?.overrideHomeVisibility ?: true
        entity.detailVisibility = operator?.overrideDetailVisibility ?: true
        entity.lastProjectionRefreshAt = OffsetDateTime.now(clock)
        servingStateRepository.save(entity)
    }

    @Transactional
    fun refreshAll() {
        influencerRepository.findAllByDeletedAtIsNull().forEach { refreshInfluencer(it.id) }
    }
}

