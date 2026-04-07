package com.mystarnow.backend.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "influencer_operator_metadata")
class InfluencerOperatorMetadataEntity(
    @Id
    @Column(name = "influencer_id", nullable = false)
    var influencerId: UUID,

    @Column(name = "override_display_name", length = 120)
    var overrideDisplayName: String? = null,

    @Column(name = "override_bio")
    var overrideBio: String? = null,

    @Column(name = "override_profile_image_url")
    var overrideProfileImageUrl: String? = null,

    @Column(name = "override_home_visibility")
    var overrideHomeVisibility: Boolean? = null,

    @Column(name = "override_detail_visibility")
    var overrideDetailVisibility: Boolean? = null,

    @Column(name = "note")
    var note: String? = null,

    @Column(name = "updated_by_operator", length = 120)
    var updatedByOperator: String? = null,
) : AuditedEntity()
