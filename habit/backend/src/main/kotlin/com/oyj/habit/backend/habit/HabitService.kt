package com.oyj.habit.backend.habit

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional(readOnly = true)
class HabitService(
    private val habitRepository: HabitRepository,
) {

    fun listHabits(): List<HabitResponse> {
        return habitRepository.findAllByOrderByCreatedAtDesc().map(HabitEntity::toResponse)
    }

    @Transactional
    fun createHabit(request: CreateHabitRequest): HabitResponse {
        val habit = HabitEntity(
            title = request.title.trim(),
            description = request.description.trim(),
            category = request.category.trim(),
            frequency = request.frequency,
        )

        return habitRepository.save(habit).toResponse()
    }

    @Transactional
    fun importHabits(request: ImportHabitsRequest): List<HabitResponse> {
        request.habits.forEach { importedHabit ->
            val habitId = parseHabitId(importedHabit.id)
            if (habitRepository.existsById(habitId)) {
                return@forEach
            }

            val habit = HabitEntity(
                id = habitId,
                title = importedHabit.title.trim(),
                description = importedHabit.description.trim(),
                category = importedHabit.category.trim(),
                frequency = importedHabit.frequency,
                createdAt = parseCreatedAt(importedHabit.createdAt),
            )

            normalizeImportedDates(
                frequency = importedHabit.frequency,
                completedDates = importedHabit.completedDates,
            ).forEach(habit::addCompletion)

            habitRepository.save(habit)
        }

        return listHabits()
    }

    @Transactional
    fun toggleCompletion(id: UUID, request: ToggleCompletionRequest): HabitResponse {
        val habit = habitRepository.findByIdWithCompletionRecords(id)
            ?: throw HabitNotFoundException(id)

        val normalizedDate = DateKeyUtils.normalize(request.date)
        habit.frequency.validateDate(normalizedDate)

        val periodKey = habit.frequency.periodKey(normalizedDate)
        if (habit.hasCompletionForPeriod(periodKey)) {
            habit.removeCompletionForPeriod(periodKey)
        } else {
            habit.addCompletion(normalizedDate)
        }

        return habit.toResponse()
    }

    @Transactional
    fun deleteHabit(id: UUID) {
        if (!habitRepository.existsById(id)) {
            throw HabitNotFoundException(id)
        }

        habitRepository.deleteById(id)
    }

    private fun normalizeImportedDates(
        frequency: HabitFrequency,
        completedDates: List<String>,
    ): List<String> {
        val seenPeriods = linkedSetOf<String>()

        return completedDates.asSequence()
            .mapNotNull { rawDate ->
                runCatching { DateKeyUtils.normalize(rawDate) }.getOrNull()
            }
            .distinct()
            .sortedDescending()
            .filter { date ->
                frequency != HabitFrequency.WEEKDAYS || DateKeyUtils.isWeekday(date)
            }
            .filter { date ->
                seenPeriods.add(frequency.periodKey(date))
            }
            .toList()
    }

    private fun parseHabitId(value: String): UUID {
        return runCatching { UUID.fromString(value) }
            .getOrElse { throw IllegalArgumentException("가져올 habit id가 UUID 형식이 아닙니다.") }
    }

    private fun parseCreatedAt(value: String): Instant {
        return runCatching { Instant.parse(value) }
            .getOrElse { throw IllegalArgumentException("createdAt 값이 ISO-8601 형식이 아닙니다.") }
    }
}
