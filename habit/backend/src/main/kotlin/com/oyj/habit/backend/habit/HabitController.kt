package com.oyj.habit.backend.habit

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/habits")
class HabitController(
    private val habitService: HabitService,
) {

    @GetMapping
    fun listHabits(): List<HabitResponse> {
        return habitService.listHabits()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createHabit(@Valid @RequestBody request: CreateHabitRequest): HabitResponse {
        return habitService.createHabit(request)
    }

    @PostMapping("/import")
    fun importHabits(@Valid @RequestBody request: ImportHabitsRequest): List<HabitResponse> {
        return habitService.importHabits(request)
    }

    @PutMapping("/{id}/completion")
    fun toggleCompletion(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ToggleCompletionRequest,
    ): HabitResponse {
        return habitService.toggleCompletion(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteHabit(@PathVariable id: UUID) {
        habitService.deleteHabit(id)
    }
}
