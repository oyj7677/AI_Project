package com.oyj.habit.backend.habit

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "habit_completions",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_habit_completion_date",
            columnNames = ["habit_id", "date_key"],
        ),
    ],
)
class HabitCompletionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id", nullable = false)
    var habit: HabitEntity,

    @Column(name = "date_key", nullable = false, length = 10)
    var dateKey: String,
)
