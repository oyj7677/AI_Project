package com.mystarnow.backend.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "influencers")
class InfluencerEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,

    @Column(name = "slug", nullable = false, unique = true, length = 120)
    var slug: String,

    @Column(name = "display_name", nullable = false, length = 120)
    var displayName: String,

    @Column(name = "normalized_name", nullable = false, length = 120)
    var normalizedName: String,

    @Column(name = "bio")
    var bio: String? = null,

    @Column(name = "profile_image_url")
    var profileImageUrl: String? = null,

    @Column(name = "status", nullable = false, length = 16)
    var status: String = "active",

    @Column(name = "is_featured", nullable = false)
    var featured: Boolean = false,

    @Column(name = "default_timezone", length = 64)
    var defaultTimezone: String? = null,

    @Column(name = "latest_activity_at")
    var latestActivityAt: OffsetDateTime? = null,

    @Column(name = "current_live_platform", length = 32)
    var currentLivePlatform: String? = null,

    @Column(name = "is_live_now", nullable = false)
    var liveNow: Boolean = false,

    @Column(name = "deleted_at")
    var deletedAt: OffsetDateTime? = null,
) : AuditedEntity()

