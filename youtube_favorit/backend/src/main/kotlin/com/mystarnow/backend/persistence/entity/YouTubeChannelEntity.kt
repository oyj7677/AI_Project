package com.mystarnow.backend.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "youtube_channels")
class YouTubeChannelEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,

    @Column(name = "platform_code", nullable = false, length = 32)
    var platformCode: String = "youtube",

    @Column(name = "external_channel_id", nullable = false, length = 255)
    var externalChannelId: String,

    @Column(name = "handle", length = 255)
    var handle: String? = null,

    @Column(name = "channel_url", nullable = false)
    var channelUrl: String,

    @Column(name = "display_label", length = 255)
    var displayLabel: String? = null,

    @Column(name = "channel_type", nullable = false, length = 32)
    var channelType: String,

    @Column(name = "owner_type", nullable = false, length = 16)
    var ownerType: String,

    @Column(name = "owner_group_id")
    var ownerGroupId: UUID? = null,

    @Column(name = "owner_member_id")
    var ownerMemberId: UUID? = null,

    @Column(name = "uploads_playlist_id", length = 255)
    var uploadsPlaylistId: String? = null,

    @Column(name = "is_official", nullable = false)
    var official: Boolean = false,

    @Column(name = "is_primary", nullable = false)
    var primary: Boolean = false,

    @Column(name = "status", nullable = false, length = 16)
    var status: String = "active",

    @Column(name = "verified_at")
    var verifiedAt: OffsetDateTime? = null,

    @Column(name = "last_seen_at")
    var lastSeenAt: OffsetDateTime? = null,

    @Column(name = "deleted_at")
    var deletedAt: OffsetDateTime? = null,
) : AuditedEntity()

