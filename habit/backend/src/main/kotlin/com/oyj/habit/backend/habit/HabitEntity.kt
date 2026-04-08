package com.oyj.habit.backend.habit

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "habits")
class HabitEntity(
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 120)
    var title: String,

    @Column(nullable = false, length = 1000)
    var description: String = "",

    @Column(nullable = false, length = 80)
    var category: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    var frequency: HabitFrequency,

    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "habit", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("dateKey DESC")
    val completionRecords: MutableList<HabitCompletionEntity> = mutableListOf(),
) {

    fun addCompletion(dateKey: String) {
        completionRecords.add(HabitCompletionEntity(habit = this, dateKey = dateKey))
    }

    fun hasCompletionForPeriod(periodKey: String): Boolean {
        return completionRecords.any { frequency.periodKey(it.dateKey) == periodKey }
    }

    fun removeCompletionForPeriod(periodKey: String) {
        completionRecords.removeIf { frequency.periodKey(it.dateKey) == periodKey }
    }
}
