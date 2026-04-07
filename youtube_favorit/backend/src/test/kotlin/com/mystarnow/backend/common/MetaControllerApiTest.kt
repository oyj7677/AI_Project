package com.mystarnow.backend.common

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mystarnow.backend.common.api.ApiResponse
import com.mystarnow.backend.common.api.FreshnessStatus
import com.mystarnow.backend.common.api.ResponseMeta
import com.mystarnow.backend.common.api.SectionStates
import com.mystarnow.backend.meta.api.MetaController
import com.mystarnow.backend.meta.service.AppConfigResponseData
import com.mystarnow.backend.meta.service.AppConfigService
import com.mystarnow.backend.meta.service.FeatureFlagsSectionData
import com.mystarnow.backend.meta.service.RuntimeConfigSectionData
import com.mystarnow.backend.meta.service.SupportedPlatformItem
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

class MetaControllerApiTest {
    private val service = mockk<AppConfigService>()
    private val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(MetaController(service))
        .setMessageConverters(
            MappingJackson2HttpMessageConverter(
                jacksonObjectMapper()
                    .registerKotlinModule()
                    .findAndRegisterModules(),
            ),
        )
        .build()

    @Test
    fun `renders runtime config envelope`() {
        every { service.getAppConfig(any(), any(), any()) } returns ApiResponse(
            meta = ResponseMeta(
                requestId = "req-config",
                generatedAt = OffsetDateTime.parse("2026-04-04T12:00:00Z"),
                partialFailure = false,
            ),
            data = AppConfigResponseData(
                runtime = SectionStates.success(
                    data = RuntimeConfigSectionData(
                        minimumSupportedAppVersion = "1.0.0",
                        defaultPageSize = 8,
                        maxPageSize = 20,
                        supportedPlatforms = listOf(
                            SupportedPlatformItem("youtube", true, "auto"),
                        ),
                    ),
                    generatedAt = OffsetDateTime.parse("2026-04-04T12:00:00Z"),
                    freshness = FreshnessStatus.manual,
                ),
                featureFlags = SectionStates.success(
                    data = FeatureFlagsSectionData(
                        showSchedules = false,
                        showRecentActivities = false,
                        enableLiveNow = false,
                        showMemberDetail = true,
                        showFeaturedGroups = true,
                        showVideoFeed = true,
                    ),
                    generatedAt = OffsetDateTime.parse("2026-04-04T12:00:00Z"),
                    freshness = FreshnessStatus.manual,
                ),
            ),
        )

        mockMvc.perform(get("/v1/meta/app-config").param("clientPlatform", "android"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meta.partialFailure").value(false))
            .andExpect(jsonPath("$.data.runtime.data.minimumSupportedAppVersion").value("1.0.0"))
            .andExpect(jsonPath("$.data.featureFlags.data.showVideoFeed").value(true))
    }
}
