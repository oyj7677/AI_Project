package com.mystarnow.backend.persistence.repository

import com.mystarnow.backend.persistence.entity.YouTubeChannelEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface YouTubeChannelRepository : JpaRepository<YouTubeChannelEntity, UUID> {
    fun findAllByOwnerGroupIdAndDeletedAtIsNull(ownerGroupId: UUID): List<YouTubeChannelEntity>

    fun findAllByOwnerGroupIdInAndDeletedAtIsNull(ownerGroupIds: Collection<UUID>): List<YouTubeChannelEntity>

    fun findAllByOwnerMemberIdAndDeletedAtIsNull(ownerMemberId: UUID): List<YouTubeChannelEntity>

    fun findAllByOwnerMemberIdInAndDeletedAtIsNull(ownerMemberIds: Collection<UUID>): List<YouTubeChannelEntity>

    fun findAllByPlatformCodeAndStatusAndDeletedAtIsNull(platformCode: String, status: String): List<YouTubeChannelEntity>

    fun findByPlatformCodeAndExternalChannelIdAndDeletedAtIsNull(
        platformCode: String,
        externalChannelId: String,
    ): YouTubeChannelEntity?
}

