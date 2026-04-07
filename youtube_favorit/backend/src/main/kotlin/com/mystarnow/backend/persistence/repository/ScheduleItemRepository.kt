package com.mystarnow.backend.persistence.repository

import com.mystarnow.backend.persistence.entity.ScheduleItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import java.util.UUID

interface ScheduleItemRepository : JpaRepository<ScheduleItemEntity, UUID> {
    fun findAllByScheduledAtBetweenAndStatusOrderByScheduledAtAsc(
        start: OffsetDateTime,
        end: OffsetDateTime,
        status: String,
    ): List<ScheduleItemEntity>

    fun findAllByInfluencerIdAndScheduledAtGreaterThanEqualOrderByScheduledAtAsc(
        influencerId: UUID,
        start: OffsetDateTime,
    ): List<ScheduleItemEntity>
}

