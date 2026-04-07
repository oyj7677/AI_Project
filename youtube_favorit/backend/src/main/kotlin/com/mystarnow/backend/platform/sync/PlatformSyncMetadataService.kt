package com.mystarnow.backend.platform.sync

import com.mystarnow.backend.persistence.entity.PlatformSyncMetadataEntity
import com.mystarnow.backend.persistence.repository.PlatformSyncMetadataRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

@Service
class PlatformSyncMetadataService(
    private val repository: PlatformSyncMetadataRepository,
    private val clock: Clock,
    private val backoffPolicy: SyncBackoffPolicy,
) {
    @Transactional
    fun getOrCreate(
        platformCode: String,
        resourceScope: String,
        syncKey: String,
        channelId: UUID?,
        influencerId: UUID?,
    ): PlatformSyncMetadataEntity =
        repository.findByPlatformCodeAndResourceScopeAndSyncKey(platformCode, resourceScope, syncKey)
            ?: repository.save(
                PlatformSyncMetadataEntity(
                    id = UUID.randomUUID(),
                    platformCode = platformCode,
                    resourceScope = resourceScope,
                    channelId = channelId,
                    influencerId = influencerId,
                    syncKey = syncKey,
                    lastStatus = SyncStatuses.IDLE,
                ),
            )

    fun shouldRun(metadata: PlatformSyncMetadataEntity): SyncDecision =
        backoffPolicy.shouldRun(metadata, OffsetDateTime.now(clock))

    @Transactional
    fun recordAttempt(metadata: PlatformSyncMetadataEntity) {
        metadata.lastAttemptedAt = OffsetDateTime.now(clock)
        repository.save(metadata)
    }

    @Transactional
    fun recordSuccess(
        metadata: PlatformSyncMetadataEntity,
        etag: String? = null,
    ) {
        val now = OffsetDateTime.now(clock)
        metadata.lastAttemptedAt = now
        metadata.lastSucceededAt = now
        metadata.lastStatus = SyncStatuses.SUCCESS
        metadata.lastErrorCode = null
        metadata.lastErrorMessage = null
        metadata.consecutiveFailures = 0
        metadata.backoffUntil = null
        metadata.nextScheduledAt = backoffPolicy.nextSuccess(metadata.resourceScope, now)
        metadata.etag = etag ?: metadata.etag
        repository.save(metadata)
    }

    @Transactional
    fun recordFailure(
        metadata: PlatformSyncMetadataEntity,
        errorCode: String,
        errorMessage: String,
    ) {
        val now = OffsetDateTime.now(clock)
        val failures = metadata.consecutiveFailures + 1
        metadata.lastAttemptedAt = now
        metadata.lastStatus = SyncStatuses.FAILED
        metadata.lastErrorCode = errorCode
        metadata.lastErrorMessage = errorMessage
        metadata.consecutiveFailures = failures
        val next = backoffPolicy.nextFailure(metadata.resourceScope, now, failures)
        metadata.backoffUntil = next
        metadata.nextScheduledAt = next
        repository.save(metadata)
    }
}

