package com.mystarnow.backend.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.UUID

@Embeddable
data class InfluencerCategoryId(
    @Column(name = "influencer_id", nullable = false)
    var influencerId: UUID = UUID.randomUUID(),

    @Column(name = "category_code", nullable = false, length = 64)
    var categoryCode: String = "",
) : Serializable

