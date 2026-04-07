package com.mystarnow.backend.meta.service

import com.mystarnow.backend.common.api.ApiResponse
import com.mystarnow.backend.common.api.FreshnessStatus
import com.mystarnow.backend.common.api.ResponseMeta
import com.mystarnow.backend.common.api.SectionStates
import com.mystarnow.backend.common.api.collectSectionErrors
import com.mystarnow.backend.common.api.hasPartialFailure
import com.mystarnow.backend.common.config.AppProperties
import com.mystarnow.backend.common.error.BadRequestException
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime

@Service
class AppConfigService(
    private val appProperties: AppProperties,
    private val clock: Clock,
) {
    fun getAppConfig(
        requestId: String,
        clientPlatform: String,
        clientVersion: String?,
    ): ApiResponse<AppConfigResponseData> {
        if (clientPlatform !in setOf("android", "web")) {
            throw BadRequestException("Invalid client platform")
        }

        val now = OffsetDateTime.now(clock)
        val runtime = SectionStates.success(
            data = RuntimeConfigSectionData(
                minimumSupportedAppVersion = "1.0.0",
                defaultPageSize = appProperties.defaultPageSize,
                maxPageSize = appProperties.maxPageSize,
                supportedPlatforms = appProperties.support.platforms.map {
                    SupportedPlatformItem(
                        platform = it.platform,
                        enabled = it.enabled,
                        supportMode = it.supportMode,
                    )
                },
            ),
            generatedAt = now,
            freshness = FreshnessStatus.manual,
        )
        val featureFlags = SectionStates.success(
            data = FeatureFlagsSectionData(
                showSchedules = appProperties.features.showSchedules,
                showRecentActivities = appProperties.features.showRecentActivities,
                enableLiveNow = appProperties.features.enableLiveNow,
                showMemberDetail = appProperties.features.showMemberDetail,
                showFeaturedGroups = appProperties.features.showFeaturedGroups,
                showVideoFeed = appProperties.features.showVideoFeed,
            ),
            generatedAt = now,
            freshness = FreshnessStatus.manual,
        )

        return ApiResponse(
            meta = ResponseMeta(
                requestId = requestId,
                generatedAt = now,
                partialFailure = hasPartialFailure(runtime, featureFlags),
            ),
            data = AppConfigResponseData(
                runtime = runtime,
                featureFlags = featureFlags,
            ),
            errors = collectSectionErrors(
                "runtime" to runtime,
                "featureFlags" to featureFlags,
            ),
        )
    }
}
