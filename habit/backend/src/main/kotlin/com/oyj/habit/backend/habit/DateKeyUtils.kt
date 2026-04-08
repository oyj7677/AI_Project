package com.oyj.habit.backend.habit

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateKeyUtils {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun normalize(dateKey: String): String {
        return LocalDate.parse(dateKey, formatter).format(formatter)
    }

    fun isWeekday(dateKey: String): Boolean {
        return isWeekday(LocalDate.parse(dateKey, formatter))
    }

    fun weekStart(dateKey: String): String {
        return weekStart(LocalDate.parse(dateKey, formatter)).format(formatter)
    }

    private fun isWeekday(date: LocalDate): Boolean {
        return date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY
    }

    private fun weekStart(date: LocalDate): LocalDate {
        val offset = when (date.dayOfWeek) {
            DayOfWeek.SUNDAY -> -6L
            else -> (DayOfWeek.MONDAY.value - date.dayOfWeek.value).toLong()
        }

        return date.plusDays(offset)
    }
}
