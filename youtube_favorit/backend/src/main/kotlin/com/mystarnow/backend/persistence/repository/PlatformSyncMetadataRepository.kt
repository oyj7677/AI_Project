package com.mystarnow.backend.persistence.repository

import com.mystarnow.backend.persistence.entity.PlatformSyncMetadataEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import java.util.UUID

interface PlatformSyncMetadataRepository : JpaRepository<PlatformSyncMetadataEntity, UUID> {
    fun findByPlatformCodeAndResourceScopeAndSyncKey(
        platformCode: String,
        resourceScope: String,
        syncKey: String,
    ): PlatformSyncMetadataEntity?

    fun findTop1ByPlatformCodeAndResourceScopeOrderByUpdatedAtDesc(
        platformCode: String,
        resourceScope: String,
    ): PlatformSyncMetadataEntity?

    fun findAllByPlatformCodeAndResourceScopeAndChannelIdIn(
        platformCode: String,
        resourceScope: String,
        channelIds: Collection<UUID>,
    ): List<PlatformSyncMetadataEntity>

    fun findAllByPlatformCodeAndResourceScopeAndNextScheduledAtLessThanEqual(
        platformCode: String,
        resourceScope: String,
        nextScheduledAt: OffsetDateTime,
    ): List<PlatformSyncMetadataEntity>

    fun countByPlatformCodeAndResourceScopeAndLastStatus(
        platformCode: String,
        resourceScope: String,
        lastStatus: String,
    ): Long
}
