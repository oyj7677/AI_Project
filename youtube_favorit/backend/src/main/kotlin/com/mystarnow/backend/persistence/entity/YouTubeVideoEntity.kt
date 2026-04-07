package com.mystarnow.backend.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "youtube_videos")
class YouTubeVideoEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,

    @Column(name = "channel_id", nullable = false)
    var channelId: UUID,

    @Column(name = "external_video_id", length = 255)
    var externalVideoId: String? = null,

    @Column(name = "title", nullable = false, length = 255)
    var title: String,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "thumbnail_url")
    var thumbnailUrl: String? = null,

    @Column(name = "published_at", nullable = false)
    var publishedAt: OffsetDateTime,

    @Column(name = "video_url", nullable = false)
    var videoUrl: String,

    @Column(name = "content_type", nullable = false, length = 32)
    var contentType: String = "video",

    @Column(name = "source_type", nullable = false, length = 32)
    var sourceType: String = "manual",

    @Column(name = "freshness_status", nullable = false, length = 16)
    var freshnessStatus: String = "manual",

    @Column(name = "stale_at")
    var staleAt: OffsetDateTime? = null,

    @Column(name = "is_pinned", nullable = false)
    var pinned: Boolean = false,

    @Column(name = "last_successful_sync_at")
    var lastSuccessfulSyncAt: OffsetDateTime? = null,

    @Column(name = "deleted_at")
    var deletedAt: OffsetDateTime? = null,
) : AuditedEntity()
