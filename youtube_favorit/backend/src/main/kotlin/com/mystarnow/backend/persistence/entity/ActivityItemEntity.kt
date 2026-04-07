package com.mystarnow.backend.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "activity_items")
class ActivityItemEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,

    @Column(name = "influencer_id", nullable = false)
    var influencerId: UUID,

    @Column(name = "channel_id")
    var channelId: UUID? = null,

    @Column(name = "platform_code", nullable = false, length = 32)
    var platformCode: String,

    @Column(name = "source_activity_id", length = 255)
    var sourceActivityId: String? = null,

    @Column(name = "source_type", nullable = false, length = 16)
    var sourceType: String,

    @Column(name = "content_type", nullable = false, length = 32)
    var contentType: String,

    @Column(name = "title", nullable = false, length = 255)
    var title: String,

    @Column(name = "summary")
    var summary: String? = null,

    @Column(name = "thumbnail_url")
    var thumbnailUrl: String? = null,

    @Column(name = "published_at", nullable = false)
    var publishedAt: OffsetDateTime,

    @Column(name = "external_url", nullable = false)
    var externalUrl: String,

    @Column(name = "is_pinned", nullable = false)
    var pinned: Boolean = false,

    @Column(name = "freshness_status", nullable = false, length = 16)
    var freshnessStatus: String,

    @Column(name = "stale_at")
    var staleAt: OffsetDateTime? = null,

    @Column(name = "source_record_id")
    var sourceRecordId: UUID? = null,

    @Column(name = "source_reference")
    var sourceReference: String? = null,

    @Column(name = "last_successful_sync_at")
    var lastSuccessfulSyncAt: OffsetDateTime? = null,
) : AuditedEntity()
