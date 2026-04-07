package com.mystarnow.backend.persistence.repository

import com.mystarnow.backend.persistence.entity.InfluencerEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface InfluencerRepository : JpaRepository<InfluencerEntity, UUID> {
    fun findAllByStatusAndDeletedAtIsNull(status: String): List<InfluencerEntity>

    fun findBySlugAndStatusAndDeletedAtIsNull(slug: String, status: String): InfluencerEntity?

    fun findBySlugAndDeletedAtIsNull(slug: String): InfluencerEntity?

    fun findAllByDeletedAtIsNull(): List<InfluencerEntity>
}
