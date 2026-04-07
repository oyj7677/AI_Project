package com.mystarnow.backend.persistence.repository

import com.mystarnow.backend.persistence.entity.LiveStatusCacheEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface LiveStatusCacheRepository : JpaRepository<LiveStatusCacheEntity, UUID> {
    fun findAllByLiveTrueOrderBySnapshotAtDesc(): List<LiveStatusCacheEntity>

    fun findAllByChannelIdIn(channelIds: Collection<UUID>): List<LiveStatusCacheEntity>

    fun countByFreshnessStatus(freshnessStatus: String): Long
}
