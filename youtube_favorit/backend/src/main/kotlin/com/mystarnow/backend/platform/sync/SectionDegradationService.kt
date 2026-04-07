package com.mystarnow.backend.platform.sync

import com.mystarnow.backend.common.api.FreshnessStatus
import com.mystarnow.backend.common.api.SectionError
import com.mystarnow.backend.common.api.SectionState
import com.mystarnow.backend.common.api.SectionStates
import com.mystarnow.backend.common.api.SectionStatus
import com.mystarnow.backend.persistence.repository.PlatformSyncMetadataRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

@Service
class SectionDegradationService(
    private val repository: PlatformSyncMetadataRepository,
    private val clock: Clock,
    private val backoffPolicy: SyncBackoffPolicy,
    private val syncMetricsService: SyncMetricsService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun <T> applyForChannels(
        sectionName: String,
        base: SectionState<T>,
        platformCode: String,
        resourceScope: String,
        channelIds: Collection<UUID>,
    ): SectionState<T> {
        if (channelIds.isEmpty()) {
            return base
        }

        val now = OffsetDateTime.now(clock)
        val degradedMetadata = repository
            .findAllByPlatformCodeAndResourceScopeAndChannelIdIn(platformCode, resourceScope, channelIds)
            .filter { backoffPolicy.isDegraded(it, now) }

        if (degradedMetadata.isEmpty()) {
            return base
        }

        val strongest = degradedMetadata.maxByOrNull { it.consecutiveFailures } ?: return base
        val error = SectionError(
            code = strongest.lastErrorCode ?: "${platformCode.uppercase()}_${resourceScope.uppercase()}_DEGRADED",
            message = strongest.lastErrorMessage
                ?: "$platformCode $resourceScope is degraded; stale or fallback data is being served.",
            retryable = true,
            source = platformCode,
        )

        syncMetricsService.recordSectionDegraded(sectionName, platformCode, resourceScope)
        log.info(
            "section degraded section={} platform={} scope={} failures={} syncKey={}",
            sectionName,
            platformCode,
            resourceScope,
            strongest.consecutiveFailures,
            strongest.syncKey,
        )

        return when (base.status) {
            SectionStatus.empty ->
                SectionStates.failed(base.data, now, error)
            SectionStatus.failed,
            SectionStatus.partial,
            -> base
            else ->
                SectionStates.partial(
                    data = base.data,
                    generatedAt = now,
                    freshness = if (base.freshness == FreshnessStatus.manual) FreshnessStatus.manual else FreshnessStatus.stale,
                    staleAt = strongest.backoffUntil ?: strongest.nextScheduledAt,
                    error = error,
                )
        }
    }
}
