package com.mystarnow.backend.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "channel_operator_metadata")
class ChannelOperatorMetadataEntity(
    @Id
    @Column(name = "channel_id", nullable = false)
    var channelId: UUID,

    @Column(name = "override_handle", length = 255)
    var overrideHandle: String? = null,

    @Column(name = "override_channel_url")
    var overrideChannelUrl: String? = null,

    @Column(name = "override_is_official")
    var overrideIsOfficial: Boolean? = null,

    @Column(name = "override_is_primary")
    var overrideIsPrimary: Boolean? = null,

    @Column(name = "note")
    var note: String? = null,

    @Column(name = "updated_by_operator", length = 120)
    var updatedByOperator: String? = null,
) : AuditedEntity()
