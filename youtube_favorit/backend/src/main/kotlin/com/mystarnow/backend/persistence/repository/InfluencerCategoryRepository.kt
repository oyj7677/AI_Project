package com.mystarnow.backend.persistence.repository

import com.mystarnow.backend.persistence.entity.InfluencerCategoryEntity
import com.mystarnow.backend.persistence.entity.InfluencerCategoryId
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface InfluencerCategoryRepository : JpaRepository<InfluencerCategoryEntity, InfluencerCategoryId> {
    fun findAllByIdInfluencerIdIn(influencerIds: Collection<UUID>): List<InfluencerCategoryEntity>
}

