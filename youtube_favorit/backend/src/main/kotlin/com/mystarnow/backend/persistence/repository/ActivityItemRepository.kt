package com.mystarnow.backend.persistence.repository

import com.mystarnow.backend.persistence.entity.ActivityItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ActivityItemRepository : JpaRepository<ActivityItemEntity, UUID> {
    fun findTop50ByOrderByPublishedAtDesc(): List<ActivityItemEntity>

    fun findAllByInfluencerIdOrderByPublishedAtDesc(influencerId: UUID): List<ActivityItemEntity>

    fun findAllByInfluencerIdInOrderByPublishedAtDesc(influencerIds: Collection<UUID>): List<ActivityItemEntity>

    fun findAllByChannelIdAndPlatformCodeAndSourceTypeOrderByPublishedAtDesc(
        channelId: UUID,
        platformCode: String,
        sourceType: String,
    ): List<ActivityItemEntity>

    fun deleteAllByChannelIdAndPlatformCodeAndSourceType(
        channelId: UUID,
        platformCode: String,
        sourceType: String,
    )

    fun countByFreshnessStatus(freshnessStatus: String): Long
}
