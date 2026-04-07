package com.mystarnow.backend.platform.sync

import com.mystarnow.backend.common.api.FreshnessStatus
import com.mystarnow.backend.common.config.AppProperties
import com.mystarnow.backend.persistence.entity.ActivityItemEntity
import com.mystarnow.backend.persistence.entity.ChannelEntity
import com.mystarnow.backend.persistence.entity.LiveStatusCacheEntity
import com.mystarnow.backend.persistence.repository.ActivityItemRepository
import com.mystarnow.backend.persistence.repository.ChannelRepository
import com.mystarnow.backend.persistence.repository.InfluencerOperatorMetadataRepository
import com.mystarnow.backend.persistence.repository.InfluencerRepository
import com.mystarnow.backend.persistence.repository.LiveStatusCacheRepository
import com.mystarnow.backend.platform.integration.SyncScopes
import com.mystarnow.backend.platform.integration.youtube.YouTubeAdapter
import com.mystarnow.backend.persistence.readmodel.InfluencerServingStateProjector
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Service
@ConditionalOnProperty(prefix = "app.features", name = ["enable-live-now"], havingValue = "true")
class YouTubeSyncService(
    private val appProperties: AppProperties,
    private val channelRepository: ChannelRepository,
    private val influencerRepository: InfluencerRepository,
    private val influencerOperatorMetadataRepository: InfluencerOperatorMetadataRepository,
    private val activityItemRepository: ActivityItemRepository,
    private val liveStatusCacheRepository: LiveStatusCacheRepository,
    private val rawSourceRecordService: RawSourceRecordService,
    private val metadataService: PlatformSyncMetadataService,
    private val projector: InfluencerServingStateProjector,
    private val backoffPolicy: SyncBackoffPolicy,
    private val syncMetricsService: SyncMetricsService,
    private val adapter: YouTubeAdapter,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun syncDueChannelProfiles() {
        syncDue(SyncScopes.CHANNEL_PROFILE) { channel ->
            val snapshot = executeWithRetry(SyncScopes.CHANNEL_PROFILE) {
                adapter.fetchChannelSnapshot(channel.externalChannelId)
            }
            val raw = rawSourceRecordService.record(
                platformCode = "youtube",
                resourceScope = SyncScopes.CHANNEL_PROFILE,
                externalObjectId = channel.externalChannelId,
                payload = snapshot,
                channelId = channel.id,
                influencerId = channel.influencerId,
                httpStatus = 200,
                requestTraceId = null,
            )
            applyChannelProfile(channel, snapshot.title, snapshot.description, snapshot.profileImageUrl)
            raw.normalizedAt = OffsetDateTime.now(clock)
            syncMetricsService.recordSync("youtube", SyncScopes.CHANNEL_PROFILE, "success")
        }
    }

    @Transactional
    fun syncDueActivities() {
        syncDue(SyncScopes.CHANNEL_ACTIVITY) { channel ->
            val snapshots = executeWithRetry(SyncScopes.CHANNEL_ACTIVITY) {
                adapter.fetchRecentActivities(channel.externalChannelId, appProperties.youtube.maxResults)
            }
            val raw = rawSourceRecordService.record(
                platformCode = "youtube",
                resourceScope = SyncScopes.CHANNEL_ACTIVITY,
                externalObjectId = channel.externalChannelId,
                payload = snapshots,
                channelId = channel.id,
                influencerId = channel.influencerId,
                httpStatus = 200,
                requestTraceId = null,
            )
            activityItemRepository.deleteAllByChannelIdAndPlatformCodeAndSourceType(channel.id, "youtube", "imported")
            val now = OffsetDateTime.now(clock)
            val activityEntities = snapshots.map {
                ActivityItemEntity(
                    id = UUID.randomUUID(),
                    influencerId = channel.influencerId,
                    channelId = channel.id,
                    platformCode = "youtube",
                    sourceActivityId = it.externalUrl,
                    sourceType = "imported",
                    contentType = it.contentType,
                    title = it.title,
                    summary = it.summary,
                    thumbnailUrl = it.thumbnailUrl,
                    publishedAt = it.publishedAt,
                    externalUrl = it.externalUrl,
                    pinned = false,
                    freshnessStatus = FreshnessStatus.fresh.name.lowercase(),
                    staleAt = now.plusMinutes(backoffPolicy.staleAfterMinutes(SyncScopes.CHANNEL_ACTIVITY)),
                    sourceRecordId = raw.id,
                    lastSuccessfulSyncAt = now,
                )
            }
            activityItemRepository.saveAll(activityEntities)
            raw.normalizedAt = now
            syncMetricsService.recordSync("youtube", SyncScopes.CHANNEL_ACTIVITY, "success")
        }
    }

    @Transactional
    fun syncDueLiveStatus() {
        syncDue(SyncScopes.CHANNEL_LIVE) { channel ->
            val snapshot = executeWithRetry(SyncScopes.CHANNEL_LIVE) {
                adapter.fetchLiveStatus(channel.externalChannelId)
            }
            val raw = rawSourceRecordService.record(
                platformCode = "youtube",
                resourceScope = SyncScopes.CHANNEL_LIVE,
                externalObjectId = channel.externalChannelId,
                payload = snapshot,
                channelId = channel.id,
                influencerId = channel.influencerId,
                httpStatus = 200,
                requestTraceId = null,
            )
            val now = OffsetDateTime.now(clock)
            val entity = liveStatusCacheRepository.findById(channel.id).orElse(
                LiveStatusCacheEntity(
                    channelId = channel.id,
                    influencerId = channel.influencerId,
                    platformCode = "youtube",
                    live = false,
                    snapshotAt = now,
                    freshnessStatus = FreshnessStatus.unknown.name.lowercase(),
                ),
            )
            entity.influencerId = channel.influencerId
            entity.platformCode = "youtube"
            entity.live = snapshot.isLive
            entity.liveTitle = snapshot.liveTitle
            entity.watchUrl = snapshot.watchUrl
            entity.viewerCount = snapshot.viewerCount
            entity.startedAt = snapshot.startedAt
            entity.snapshotAt = now
            entity.staleAt = now.plusMinutes(backoffPolicy.staleAfterMinutes(SyncScopes.CHANNEL_LIVE))
            entity.freshnessStatus = FreshnessStatus.fresh.name.lowercase()
            entity.lastSuccessfulSyncAt = now
            entity.lastAttemptedSyncAt = now
            entity.sourceRecordId = raw.id
            entity.errorCode = null
            entity.errorMessage = null
            liveStatusCacheRepository.save(entity)
            raw.normalizedAt = now
            syncMetricsService.recordSync("youtube", SyncScopes.CHANNEL_LIVE, "success")
        }
    }

    private fun syncDue(
        scope: String,
        handler: (ChannelEntity) -> Unit,
    ) {
        channelRepository.findAllByPlatformCodeAndStatusAndDeletedAtIsNull("youtube", "active")
            .forEach { channel ->
                val metadata = metadataService.getOrCreate(
                    platformCode = "youtube",
                    resourceScope = scope,
                    syncKey = "youtube:channel:${channel.externalChannelId}:${scopeSuffix(scope)}",
                    channelId = channel.id,
                    influencerId = channel.influencerId,
                )
                val decision = metadataService.shouldRun(metadata)
                if (!decision.shouldRun) {
                    return@forEach
                }
                metadataService.recordAttempt(metadata)
                try {
                    handler(channel)
                    metadataService.recordSuccess(metadata)
                    projector.refreshInfluencer(channel.influencerId)
                } catch (ex: Exception) {
                    markStale(channel, scope, ex)
                    metadataService.recordFailure(metadata, "YOUTUBE_SYNC_FAILED", ex.message ?: "sync failed")
                    syncMetricsService.recordSync("youtube", scope, "failed")
                    projector.refreshInfluencer(channel.influencerId)
                }
            }
    }

    private fun applyChannelProfile(
        channel: ChannelEntity,
        title: String,
        description: String?,
        profileImageUrl: String?,
    ) {
        channel.displayLabel = title
        channel.lastSeenAt = OffsetDateTime.now(clock)
        channelRepository.save(channel)

        val influencer = influencerRepository.findById(channel.influencerId).orElse(null) ?: return
        val operator = influencerOperatorMetadataRepository.findById(channel.influencerId).orElse(null)
        if (operator?.overrideBio == null && !description.isNullOrBlank()) {
            influencer.bio = description
        }
        if (operator?.overrideProfileImageUrl == null && !profileImageUrl.isNullOrBlank()) {
            influencer.profileImageUrl = profileImageUrl
        }
        influencerRepository.save(influencer)
    }

    private fun markStale(
        channel: ChannelEntity,
        scope: String,
        ex: Exception,
    ) {
        val now = OffsetDateTime.now(clock)
        when (scope) {
            SyncScopes.CHANNEL_LIVE -> {
                liveStatusCacheRepository.findById(channel.id).ifPresent {
                    it.freshnessStatus = FreshnessStatus.stale.name.lowercase()
                    it.staleAt = now
                    it.lastAttemptedSyncAt = now
                    it.errorCode = "YOUTUBE_SYNC_FAILED"
                    it.errorMessage = ex.message
                    liveStatusCacheRepository.save(it)
                }
            }
            SyncScopes.CHANNEL_ACTIVITY -> {
                val items = activityItemRepository.findAllByChannelIdAndPlatformCodeAndSourceTypeOrderByPublishedAtDesc(
                    channel.id,
                    "youtube",
                    "imported",
                )
                if (items.isNotEmpty()) {
                    items.forEach {
                        it.freshnessStatus = FreshnessStatus.stale.name.lowercase()
                        it.staleAt = now
                    }
                    activityItemRepository.saveAll(items)
                }
            }
        }
        log.warn(
            "youtube sync failed scope={} channelId={} influencerId={} message={}",
            scope,
            channel.id,
            channel.influencerId,
            ex.message,
        )
    }

    private fun <T> executeWithRetry(
        scope: String,
        block: () -> T,
    ): T {
        var lastFailure: Throwable? = null
        for (attempt in 0 until appProperties.reliability.maxRetryAttempts) {
            try {
                return CompletableFuture.supplyAsync(block)
                    .orTimeout(appProperties.youtube.apiTimeoutMs, TimeUnit.MILLISECONDS)
                    .join()
            } catch (ex: Exception) {
                lastFailure = ex.cause ?: ex
            }
        }
        throw lastFailure ?: TimeoutException("YouTube $scope execution failed")
    }

    private fun scopeSuffix(scope: String): String = when (scope) {
        SyncScopes.CHANNEL_PROFILE -> "profile"
        SyncScopes.CHANNEL_ACTIVITY -> "activity"
        SyncScopes.CHANNEL_LIVE -> "live"
        else -> scope
    }
}
