package com.mystarnow.backend.persistence.repository

import com.mystarnow.backend.persistence.entity.InfluencerServingStateEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface InfluencerServingStateRepository : JpaRepository<InfluencerServingStateEntity, UUID> {
    fun findAllByInfluencerIdIn(influencerIds: Collection<UUID>): List<InfluencerServingStateEntity>
}

