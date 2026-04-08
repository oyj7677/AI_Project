package com.oyj.habit.backend.habit

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:habit-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    ],
)
class HabitControllerIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val habitRepository: HabitRepository,
) {
    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun cleanUp() {
        habitRepository.deleteAll()
    }

    @Test
    fun `create toggle list and delete habit`() {
        val createResponse = mockMvc.perform(
            post("/api/habits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "title": "물 마시기",
                      "description": "하루 2리터 이상",
                      "category": "건강",
                      "frequency": "daily"
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("물 마시기"))
            .andReturn()

        val createdHabit = objectMapper.readTree(createResponse.response.contentAsString)
        val habitId = createdHabit["id"].asText()

        mockMvc.perform(get("/api/habits"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(habitId))

        mockMvc.perform(
            put("/api/habits/$habitId/completion")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"date":"2026-04-08"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.completedDates[0]").value("2026-04-08"))

        mockMvc.perform(
            put("/api/habits/$habitId/completion")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"date":"2026-04-08"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.completedDates").isEmpty())

        mockMvc.perform(delete("/api/habits/$habitId"))
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/api/habits"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isEmpty())
    }

    @Test
    fun `import removes duplicate weekly completions from same week`() {
        val importResponse = mockMvc.perform(
            post("/api/habits/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "habits": [
                        {
                          "id": "6ab1baf4-e1bc-4900-ab1b-13206ae67018",
                          "title": "주간 리뷰",
                          "description": "매주 회고 남기기",
                          "category": "학습",
                          "frequency": "weekly",
                          "createdAt": "2026-04-01T09:00:00Z",
                          "completedDates": ["2026-04-08", "2026-04-06", "2026-04-01"]
                        }
                      ]
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andReturn()

        val importedHabits = objectMapper.readTree(importResponse.response.contentAsString)
        val completedDates = importedHabits[0]["completedDates"]

        assertEquals(2, completedDates.size())
        assertEquals("2026-04-08", completedDates[0].asText())
        assertEquals("2026-04-01", completedDates[1].asText())
    }
}
