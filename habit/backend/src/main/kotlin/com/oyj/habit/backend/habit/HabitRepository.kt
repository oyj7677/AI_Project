package com.oyj.habit.backend.habit

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface HabitRepository : JpaRepository<HabitEntity, UUID> {

    @EntityGraph(attributePaths = ["completionRecords"])
    fun findAllByOrderByCreatedAtDesc(): List<HabitEntity>

    @EntityGraph(attributePaths = ["completionRecords"])
    @Query("select habit from HabitEntity habit where habit.id = :id")
    fun findByIdWithCompletionRecords(id: UUID): HabitEntity?
}
