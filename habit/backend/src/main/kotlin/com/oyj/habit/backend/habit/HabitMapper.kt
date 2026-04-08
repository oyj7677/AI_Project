package com.oyj.habit.backend.habit

fun HabitEntity.toResponse(): HabitResponse {
    return HabitResponse(
        id = id.toString(),
        title = title,
        description = description,
        category = category,
        frequency = frequency.toWireValue(),
        createdAt = createdAt.toString(),
        completedDates = completionRecords
            .map { it.dateKey }
            .distinct()
            .sortedDescending(),
    )
}
