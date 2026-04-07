package com.mystarnow.backend.persistence.repository

import com.mystarnow.backend.persistence.entity.IdolGroupEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IdolGroupRepository : JpaRepository<IdolGroupEntity, UUID> {
    fun findAllByStatusAndDeletedAtIsNull(status: String): List<IdolGroupEntity>

    fun findBySlugAndDeletedAtIsNull(slug: String): IdolGroupEntity?

    fun findBySlugAndStatusAndDeletedAtIsNull(slug: String, status: String): IdolGroupEntity?
}

