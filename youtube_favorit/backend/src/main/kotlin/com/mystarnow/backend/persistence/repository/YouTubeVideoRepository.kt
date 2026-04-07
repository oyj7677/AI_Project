package com.mystarnow.backend.persistence.repository

import com.mystarnow.backend.persistence.entity.YouTubeVideoEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface YouTubeVideoRepository : JpaRepository<YouTubeVideoEntity, UUID> {
    fun findAllByDeletedAtIsNullOrderByPublishedAtDesc(): List<YouTubeVideoEntity>

    fun findAllByChannelIdAndDeletedAtIsNullOrderByPublishedAtDesc(channelId: UUID): List<YouTubeVideoEntity>

    fun findAllByChannelIdInAndDeletedAtIsNullOrderByPublishedAtDesc(channelIds: Collection<UUID>): List<YouTubeVideoEntity>

    fun findByExternalVideoIdAndDeletedAtIsNull(externalVideoId: String): YouTubeVideoEntity?
}
