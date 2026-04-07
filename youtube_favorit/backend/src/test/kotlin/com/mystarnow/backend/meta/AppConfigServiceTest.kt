package com.mystarnow.backend.meta

import com.mystarnow.backend.common.config.AppProperties
import com.mystarnow.backend.common.config.FeatureFlagsProperties
import com.mystarnow.backend.common.config.PlatformSupportEntry
import com.mystarnow.backend.common.config.PlatformSupportProperties
import com.mystarnow.backend.meta.service.AppConfigService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class AppConfigServiceTest {
    @Test
    fun `returns supported platforms and feature flags`() {
        val service = AppConfigService(
            appProperties = AppProperties(
                baseUrl = "http://localhost:8080",
                timezone = "Asia/Seoul",
                defaultPageSize = 8,
                maxPageSize = 20,
                support = PlatformSupportProperties(
                    platforms = listOf(
                        PlatformSupportEntry("youtube", true, "auto"),
                    ),
                ),
                features = FeatureFlagsProperties(
                    showSchedules = false,
                    showRecentActivities = false,
                    enableLiveNow = false,
                    showMemberDetail = true,
                    showFeaturedGroups = true,
                    showVideoFeed = true,
                ),
            ),
            clock = Clock.fixed(Instant.parse("2026-04-04T12:00:00Z"), ZoneOffset.UTC),
        )

        val response = service.getAppConfig("req-cfg", "web", "1.0.0")
        val data = requireNotNull(response.data)

        assertEquals(1, data.runtime.data.supportedPlatforms.size)
        assertEquals(false, data.featureFlags.data.showRecentActivities)
        assertEquals(true, data.featureFlags.data.showVideoFeed)
    }
}
