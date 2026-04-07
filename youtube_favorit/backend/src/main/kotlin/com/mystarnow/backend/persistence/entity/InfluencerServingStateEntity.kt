package com.mystarnow.backend.persistence.entity

import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "influencer_serving_state")
class InfluencerServingStateEntity(
    @Id
    @Column(name = "influencer_id", nullable = false)
    var influencerId: UUID,

    @Column(name = "is_live_now", nullable = false)
    var liveNow: Boolean = false,

    @Column(name = "live_platform_code", length = 32)
    var livePlatformCode: String? = null,

    @Column(name = "live_started_at")
    var liveStartedAt: OffsetDateTime? = null,

    @Column(name = "latest_activity_at")
    var latestActivityAt: OffsetDateTime? = null,

    @Column(name = "latest_schedule_at")
    var latestScheduleAt: OffsetDateTime? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "supported_platforms_cache", nullable = false, columnDefinition = "jsonb")
    var supportedPlatformsCache: String = "[]",

    @Column(name = "featured_rank")
    var featuredRank: Int? = null,

    @Column(name = "home_visibility", nullable = false)
    var homeVisibility: Boolean = true,

    @Column(name = "detail_visibility", nullable = false)
    var detailVisibility: Boolean = true,

    @Column(name = "last_projection_refresh_at", nullable = false)
    var lastProjectionRefreshAt: OffsetDateTime,
)
