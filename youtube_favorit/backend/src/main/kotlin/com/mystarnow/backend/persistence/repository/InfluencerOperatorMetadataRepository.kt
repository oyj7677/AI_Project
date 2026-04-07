package com.mystarnow.backend.persistence.repository

import com.mystarnow.backend.persistence.entity.InfluencerOperatorMetadataEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface InfluencerOperatorMetadataRepository : JpaRepository<InfluencerOperatorMetadataEntity, UUID> {
    fun findAllByInfluencerIdIn(influencerIds: Collection<UUID>): List<InfluencerOperatorMetadataEntity>
}

