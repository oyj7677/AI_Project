package com.mystarnow.backend.idol.platform.youtube

import com.mystarnow.backend.common.config.AppProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.OffsetDateTime

@Component
@ConditionalOnProperty(prefix = "app.youtube", name = ["mock-enabled"], havingValue = "true", matchIfMissing = true)
class MockIdolYouTubeAdapter(
    private val appProperties: AppProperties,
    private val clock: Clock,
) : IdolYouTubeAdapter {
    override fun fetchChannelProfile(channelReference: String): IdolYouTubeChannelSnapshot =
        IdolYouTubeChannelSnapshot(
            resolvedChannelId = if (channelReference.startsWith("UC")) channelReference else "UC-$channelReference",
            title = "Mock Channel $channelReference",
            description = "Mock YouTube channel",
            profileImageUrl = "https://cdn.mystarnow.dev/youtube/$channelReference.png",
            uploadsPlaylistId = "UU-$channelReference",
        )

    override fun fetchRecentVideos(
        uploadsPlaylistId: String,
        maxResults: Int,
    ): List<IdolYouTubeVideoSnapshot> {
        val now = OffsetDateTime.now(clock)
        return (1..maxResults.coerceAtMost(appProperties.youtube.maxResults)).map { index ->
            IdolYouTubeVideoSnapshot(
                externalVideoId = "$uploadsPlaylistId-$index",
                title = "Mock Video $index",
                description = "Generated from mock adapter",
                thumbnailUrl = "https://img.youtube.com/vi/mock-$index/hqdefault.jpg",
                publishedAt = now.minusHours(index.toLong()),
                videoUrl = "https://youtube.com/watch?v=$uploadsPlaylistId-$index",
                contentType = if (index % 3 == 0) "short" else "video",
            )
        }
    }
}
