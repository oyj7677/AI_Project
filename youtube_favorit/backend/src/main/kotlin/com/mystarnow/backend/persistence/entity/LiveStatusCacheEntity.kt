package com.mystarnow.backend.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Entity
@Table(name = "live_status_cache")
class LiveStatusCacheEntity(
    @Id
    @Column(name = "channel_id", nullable = false)
    var channelId: UUID,

    @Column(name = "influencer_id", nullable = false)
    var influencerId: UUID,

    @Column(name = "platform_code", nullable = false, length = 32)
    var platformCode: String,

    @Column(name = "is_live", nullable = false)
    var live: Boolean,

    @Column(name = "live_title")
    var liveTitle: String? = null,

    @Column(name = "watch_url")
    var watchUrl: String? = null,

    @Column(name = "viewer_count")
    var viewerCount: Int? = null,

    @Column(name = "started_at")
    var startedAt: OffsetDateTime? = null,

    @Column(name = "snapshot_at", nullable = false)
    var snapshotAt: OffsetDateTime,

    @Column(name = "stale_at")
    var staleAt: OffsetDateTime? = null,

    @Column(name = "freshness_status", nullable = false, length = 16)
    var freshnessStatus: String,

    @Column(name = "last_successful_sync_at")
    var lastSuccessfulSyncAt: OffsetDateTime? = null,

    @Column(name = "last_attempted_sync_at")
    var lastAttemptedSyncAt: OffsetDateTime? = null,

    @Column(name = "source_record_id")
    var sourceRecordId: UUID? = null,

    @Column(name = "error_code", length = 120)
    var errorCode: String? = null,

    @Column(name = "error_message")
    var errorMessage: String? = null,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
)
