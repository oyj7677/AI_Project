package com.mystarnow.backend.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "schedule_items")
class ScheduleItemEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,

    @Column(name = "influencer_id", nullable = false)
    var influencerId: UUID,

    @Column(name = "channel_id")
    var channelId: UUID? = null,

    @Column(name = "platform_code", length = 32)
    var platformCode: String? = null,

    @Column(name = "source_type", nullable = false, length = 16)
    var sourceType: String = "manual",

    @Column(name = "status", nullable = false, length = 16)
    var status: String = "scheduled",

    @Column(name = "title", nullable = false, length = 255)
    var title: String,

    @Column(name = "note")
    var note: String? = null,

    @Column(name = "scheduled_at", nullable = false)
    var scheduledAt: OffsetDateTime,

    @Column(name = "ends_at")
    var endsAt: OffsetDateTime? = null,

    @Column(name = "source_reference")
    var sourceReference: String? = null,

    @Column(name = "created_by_operator", length = 120)
    var createdByOperator: String? = null,

    @Column(name = "updated_by_operator", length = 120)
    var updatedByOperator: String? = null,
) : AuditedEntity()

