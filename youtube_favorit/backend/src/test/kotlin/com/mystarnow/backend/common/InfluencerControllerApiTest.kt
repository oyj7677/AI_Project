package com.mystarnow.backend.common

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mystarnow.backend.common.api.ApiResponse
import com.mystarnow.backend.common.api.ResponseMeta
import com.mystarnow.backend.common.api.SectionStates
import com.mystarnow.backend.influencer.api.InfluencerController
import com.mystarnow.backend.influencer.service.AppliedFilters
import com.mystarnow.backend.influencer.service.FilterPlatformItem
import com.mystarnow.backend.influencer.service.InfluencerFiltersSectionData
import com.mystarnow.backend.influencer.service.InfluencerListItem
import com.mystarnow.backend.influencer.service.InfluencerListResponseData
import com.mystarnow.backend.influencer.service.InfluencerQueryService
import com.mystarnow.backend.influencer.service.InfluencerResultsSectionData
import com.mystarnow.backend.influencer.service.PageInfo
import com.mystarnow.backend.influencer.service.SortOptionItem
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.OffsetDateTime

class InfluencerControllerApiTest {
    private val service = mockk<InfluencerQueryService>()
    private val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(InfluencerController(service))
        .setMessageConverters(
            MappingJackson2HttpMessageConverter(
                jacksonObjectMapper()
                    .registerKotlinModule()
                    .findAndRegisterModules(),
            ),
        )
        .build()

    @Test
    fun `renders influencer list envelope`() {
        every { service.getInfluencers(any(), any(), any(), any(), any(), any(), any()) } returns ApiResponse(
            meta = ResponseMeta(
                requestId = "req-list",
                generatedAt = OffsetDateTime.parse("2026-04-04T12:00:00Z"),
                partialFailure = false,
            ),
            data = InfluencerListResponseData(
                results = SectionStates.success(
                    data = InfluencerResultsSectionData(
                        items = listOf(
                            InfluencerListItem(
                                influencerId = "inf_haru",
                                slug = "haru",
                                name = "하루",
                                summary = "게임, 토크 중심 스트리머",
                                profileImageUrl = null,
                                categories = listOf("game", "talk"),
                                platforms = listOf("youtube", "instagram"),
                                liveStatus = "live",
                                recentActivityAt = "2026-04-04T11:00:00Z",
                                badges = listOf("featured"),
                            ),
                        ),
                        pageInfo = PageInfo(limit = 20, nextCursor = "cursor_2", hasNext = true),
                        appliedFilters = AppliedFilters(
                            q = "하루",
                            category = null,
                            platform = listOf("youtube"),
                            sort = "featured",
                        ),
                    ),
                    generatedAt = OffsetDateTime.parse("2026-04-04T12:00:00Z"),
                ),
                filters = SectionStates.success(
                    data = InfluencerFiltersSectionData(
                        categories = emptyList(),
                        platforms = listOf(FilterPlatformItem("youtube", "YouTube", true)),
                        sortOptions = listOf(SortOptionItem("featured", "추천순")),
                    ),
                    generatedAt = OffsetDateTime.parse("2026-04-04T12:00:00Z"),
                ),
            ),
            errors = emptyList(),
        )

        mockMvc.perform(get("/v1/influencers").queryParam("q", "하루"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meta.partialFailure").value(false))
            .andExpect(jsonPath("$.data.results.data.items[0].slug").value("haru"))
            .andExpect(jsonPath("$.data.results.data.pageInfo.hasNext").value(true))
    }
}
