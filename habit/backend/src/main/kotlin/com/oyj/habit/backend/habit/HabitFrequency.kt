package com.oyj.habit.backend.habit

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class HabitFrequency(
    private val wireValue: String,
) {
    DAILY("daily"),
    WEEKDAYS("weekdays"),
    WEEKLY("weekly"),
    ;

    @JsonValue
    fun toWireValue(): String = wireValue

    fun periodKey(dateKey: String): String {
        return when (this) {
            WEEKLY -> DateKeyUtils.weekStart(dateKey)
            DAILY, WEEKDAYS -> DateKeyUtils.normalize(dateKey)
        }
    }

    fun validateDate(dateKey: String) {
        if (this == WEEKDAYS && !DateKeyUtils.isWeekday(dateKey)) {
            throw IllegalArgumentException("평일 습관은 월요일부터 금요일까지만 체크할 수 있습니다.")
        }
    }

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromWireValue(value: String): HabitFrequency {
            return entries.firstOrNull { it.wireValue.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("지원하지 않는 반복 주기입니다: $value")
        }
    }
}
