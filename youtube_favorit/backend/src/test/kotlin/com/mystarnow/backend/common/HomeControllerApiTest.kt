package com.mystarnow.backend.common

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mystarnow.backend.common.api.ApiError
import com.mystarnow.backend.common.api.ApiResponse
import com.mystarnow.backend.common.api.FreshnessStatus
import com.mystarnow.backend.common.api.ResponseMeta
import com.mystarnow.backend.common.api.SectionError
import com.mystarnow.backend.common.api.SectionStates
import com.mystarnow.backend.home.api.HomeController
import com.mystarnow.backend.home.service.HomeFeaturedGroupItemView
import com.mystarnow.backend.home.service.HomeFeaturedGroupsSectionData
import com.mystarnow.backend.home.service.HomeQueryService
import com.mystarnow.backend.home.service.HomeRecentVideosSectionData
import com.mystarnow.backend.home.service.HomeResponseData
import com.mystarnow.backend.idol.service.ChannelSummaryView
import com.mystarnow.backend.idol.service.GroupRefView
import com.mystarnow.backend.idol.service.PageInfo
import com.mystarnow.backend.idol.service.VideoFeedItemView
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

class HomeControllerApiTest {
    private val service = mockk<HomeQueryService>()
    private val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(HomeController(service))
        .setMessageConverters(
            MappingJackson2HttpMessageConverter(
                jacksonObjectMapper()
                    .registerKotlinModule()
                    .findAndRegisterModules(),
            ),
        )
        .build()

    @Test
    fun `renders group-centric home envelope`() {
        every { service.getHome(any(), any(), any(), any()) } returns ApiResponse(
            meta = ResponseMeta(
                requestId = "req-home",
                generatedAt = OffsetDateTime.parse("2026-04-05T10:00:00Z"),
                partialFailure = true,
            ),
            data = HomeResponseData(
                recentVideos = SectionStates.partial(
                    data = HomeRecentVideosSectionData(
                        items = listOf(
                            VideoFeedItemView(
                                videoId = "video-1",
                                externalVideoId = "yt-1",
                                title = "최신 영상",
                                description = null,
                                thumbnailUrl = null,
                                publishedAt = "2026-04-05T08:30:00Z",
                                videoUrl = "https://youtube.com/watch?v=yt-1",
                                contentType = "video",
                                channel = ChannelSummaryView("channel-1", "UC1", "StarWave Official", "@starwave", "https://youtube.com/@starwave", "GROUP_OFFICIAL", true),
                                group = GroupRefView("group-1", "starwave", "StarWave"),
                                member = null,
                                badges = listOf("group-official"),
                            ),
                        ),
                        pageInfo = PageInfo(8, null, false),
                    ),
                    generatedAt = OffsetDateTime.parse("2026-04-05T10:00:00Z"),
                    freshness = FreshnessStatus.stale,
                    error = SectionError("YOUTUBE_SYNC_STALE", "일부 채널 동기화가 지연되었습니다.", true, "youtube"),
                ),
                featuredGroups = SectionStates.success(
                    HomeFeaturedGroupsSectionData(
                        items = listOf(
                            HomeFeaturedGroupItemView("group-1", "starwave", "StarWave", null, 1, 2, "2026-04-05T08:30:00Z"),
                        ),
                    ),
                    OffsetDateTime.parse("2026-04-05T10:00:00Z"),
                    FreshnessStatus.manual,
                ),
            ),
            errors = listOf(
                ApiError("section", "recentVideos", "YOUTUBE_SYNC_STALE", "일부 채널 동기화가 지연되었습니다."),
            ),
        )

        mockMvc.perform(get("/v1/home").param("limit", "8"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meta.partialFailure").value(true))
            .andExpect(jsonPath("$.data.recentVideos.status").value("partial"))
            .andExpect(jsonPath("$.data.recentVideos.data.items[0].group.groupSlug").value("starwave"))
            .andExpect(jsonPath("$.data.featuredGroups.data.items[0].groupSlug").value("starwave"))
    }
}
