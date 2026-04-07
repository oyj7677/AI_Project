package com.mystarnow.backend.persistence.repository

import com.mystarnow.backend.persistence.entity.ChannelEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ChannelRepository : JpaRepository<ChannelEntity, UUID> {
    fun findAllByInfluencerIdInAndDeletedAtIsNull(influencerIds: Collection<UUID>): List<ChannelEntity>

    fun findAllByInfluencerIdAndDeletedAtIsNull(influencerId: UUID): List<ChannelEntity>

    fun findAllByPlatformCodeAndStatusAndDeletedAtIsNull(platformCode: String, status: String): List<ChannelEntity>

    fun findByPlatformCodeAndExternalChannelIdAndDeletedAtIsNull(
        platformCode: String,
        externalChannelId: String,
    ): ChannelEntity?
}
