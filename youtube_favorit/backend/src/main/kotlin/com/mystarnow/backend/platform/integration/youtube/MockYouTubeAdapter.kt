package com.mystarnow.backend.platform.integration.youtube

import com.mystarnow.backend.common.config.AppProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.OffsetDateTime

@Component
@ConditionalOnProperty(prefix = "app.youtube", name = ["mock-enabled"], havingValue = "true", matchIfMissing = true)
class MockYouTubeAdapter(
    private val appProperties: AppProperties,
    private val clock: Clock,
) : YouTubeAdapter {
    override fun fetchChannelSnapshot(externalChannelId: String): YouTubeChannelSnapshot =
        YouTubeChannelSnapshot(
            externalChannelId = externalChannelId,
            title = "Mock Channel $externalChannelId",
            description = "Mock channel seeded for local/dev execution",
            profileImageUrl = "https://cdn.mystarnow.dev/youtube/$externalChannelId.png",
        )

    override fun fetchRecentActivities(externalChannelId: String, maxResults: Int): List<YouTubeActivitySnapshot> {
        val now = OffsetDateTime.now(clock)
        return (1..maxResults.coerceAtMost(appProperties.youtube.maxResults)).map { index ->
            YouTubeActivitySnapshot(
                contentType = if (index % 3 == 0) "short" else "video",
                title = "Mock YouTube Activity $index",
                summary = "Generated from mock adapter for $externalChannelId",
                thumbnailUrl = "https://img.youtube.com/vi/mock-$index/hqdefault.jpg",
                publishedAt = now.minusHours(index.toLong()),
                externalUrl = "https://youtube.com/watch?v=$externalChannelId-$index",
            )
        }
    }

    override fun fetchLiveStatus(externalChannelId: String): YouTubeLiveStatusSnapshot {
        val now = OffsetDateTime.now(clock)
        val isLive = externalChannelId.hashCode() % 2 == 0
        return YouTubeLiveStatusSnapshot(
            isLive = isLive,
            liveTitle = if (isLive) "Mock live stream for $externalChannelId" else null,
            watchUrl = if (isLive) "https://youtube.com/watch?v=$externalChannelId-live" else null,
            viewerCount = if (isLive) 1234 else null,
            startedAt = if (isLive) now.minusMinutes(25) else null,
        )
    }
}

