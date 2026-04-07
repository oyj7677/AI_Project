package com.mystarnow.backend.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AppProperties::class)
class AppPropertiesConfiguration

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val baseUrl: String,
    val timezone: String,
    val defaultPageSize: Int,
    val maxPageSize: Int,
    val sampleData: SampleDataProperties = SampleDataProperties(false),
    val features: FeatureFlagsProperties = FeatureFlagsProperties(),
    val support: PlatformSupportProperties = PlatformSupportProperties(emptyList()),
    val reliability: ReliabilityProperties = ReliabilityProperties(),
    val youtube: YouTubeProperties = YouTubeProperties(),
    val instagram: InstagramProperties = InstagramProperties(),
    val operatorAuth: OperatorAuthProperties = OperatorAuthProperties(),
)

data class SampleDataProperties(
    val enabled: Boolean,
)

data class FeatureFlagsProperties(
    val showSchedules: Boolean = true,
    val showRecentActivities: Boolean = true,
    val enableLiveNow: Boolean = true,
    val showMemberDetail: Boolean = true,
    val showFeaturedGroups: Boolean = true,
    val showVideoFeed: Boolean = true,
)

data class PlatformSupportProperties(
    val platforms: List<PlatformSupportEntry>,
)

data class PlatformSupportEntry(
    val platform: String,
    val enabled: Boolean,
    val supportMode: String,
)

data class ReliabilityProperties(
    val liveStaleAfterMinutes: Long = 5,
    val activityStaleAfterMinutes: Long = 30,
    val profileStaleAfterMinutes: Long = 360,
    val maxRetryAttempts: Int = 2,
    val liveBackoffMultiplier: Int = 2,
    val activityBackoffMultiplier: Int = 2,
    val profileBackoffMultiplier: Int = 2,
)

data class YouTubeProperties(
    val apiKey: String = "",
    val apiBaseUrl: String = "",
    val apiTimeoutMs: Long = 3000,
    val maxResults: Int = 20,
    val channelMetadataPollMinutes: Long = 720,
    val activityPollMinutes: Long = 15,
    val livePollMinutes: Long = 3,
    val enableEtag: Boolean = true,
    val syncEnabled: Boolean = false,
    val mockEnabled: Boolean = true,
)

data class InstagramProperties(
    val enabled: Boolean = false,
    val apiBaseUrl: String = "",
    val timeoutMs: Long = 3000,
)

data class OperatorAuthProperties(
    val enabled: Boolean = false,
    val username: String = "operator",
    val password: String = "operator",
)
