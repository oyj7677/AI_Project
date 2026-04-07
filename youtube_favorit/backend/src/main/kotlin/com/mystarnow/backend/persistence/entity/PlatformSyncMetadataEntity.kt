package com.mystarnow.backend.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "platform_sync_metadata")
class PlatformSyncMetadataEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,

    @Column(name = "platform_code", nullable = false, length = 32)
    var platformCode: String,

    @Column(name = "resource_scope", nullable = false, length = 32)
    var resourceScope: String,

    @Column(name = "channel_id")
    var channelId: UUID? = null,

    @Column(name = "influencer_id")
    var influencerId: UUID? = null,

    @Column(name = "sync_key", nullable = false, length = 255)
    var syncKey: String,

    @Column(name = "last_attempted_at")
    var lastAttemptedAt: OffsetDateTime? = null,

    @Column(name = "last_succeeded_at")
    var lastSucceededAt: OffsetDateTime? = null,

    @Column(name = "last_status", nullable = false, length = 16)
    var lastStatus: String = "idle",

    @Column(name = "last_error_code", length = 120)
    var lastErrorCode: String? = null,

    @Column(name = "last_error_message")
    var lastErrorMessage: String? = null,

    @Column(name = "consecutive_failures", nullable = false)
    var consecutiveFailures: Int = 0,

    @Column(name = "next_scheduled_at")
    var nextScheduledAt: OffsetDateTime? = null,

    @Column(name = "backoff_until")
    var backoffUntil: OffsetDateTime? = null,

    @Column(name = "cursor_token")
    var cursorToken: String? = null,

    @Column(name = "etag")
    var etag: String? = null,

    @Column(name = "source_quota_bucket", length = 120)
    var sourceQuotaBucket: String? = null,
) : AuditedEntity()

