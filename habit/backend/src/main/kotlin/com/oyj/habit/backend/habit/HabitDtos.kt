package com.oyj.habit.backend.habit

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class CreateHabitRequest(
    @field:NotBlank(message = "습관 제목을 입력해 주세요.")
    val title: String,

    val description: String = "",

    @field:NotBlank(message = "카테고리를 선택해 주세요.")
    val category: String,

    val frequency: HabitFrequency,
)

data class ToggleCompletionRequest(
    @field:Pattern(
        regexp = "\\d{4}-\\d{2}-\\d{2}",
        message = "날짜는 yyyy-MM-dd 형식이어야 합니다.",
    )
    val date: String,
)

data class ImportHabitsRequest(
    val habits: List<@Valid ImportedHabitRequest> = emptyList(),
)

data class ImportedHabitRequest(
    @field:NotBlank(message = "가져올 habit id가 필요합니다.")
    val id: String,

    @field:NotBlank(message = "가져올 습관 제목이 비어 있습니다.")
    val title: String,

    val description: String = "",

    @field:NotBlank(message = "가져올 카테고리가 비어 있습니다.")
    val category: String,

    val frequency: HabitFrequency,

    @field:NotBlank(message = "가져올 생성일이 필요합니다.")
    val createdAt: String,

    val completedDates: List<String> = emptyList(),
)

data class HabitResponse(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val frequency: String,
    val createdAt: String,
    val completedDates: List<String>,
)
