package com.mystarnow.backend.idol.platform.youtube

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.mystarnow.backend.common.config.AppProperties
import com.mystarnow.backend.common.error.BadRequestException
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.OffsetDateTime
import java.net.URI
import java.time.Duration

@Component
@ConditionalOnProperty(prefix = "app.youtube", name = ["mock-enabled"], havingValue = "false")
class RealIdolYouTubeAdapter(
    private val appProperties: AppProperties,
    private val webClient: WebClient,
) : IdolYouTubeAdapter {
    override fun fetchChannelProfile(channelReference: String): IdolYouTubeChannelSnapshot {
        requireApiKey()
        val baseUri = youtubeBaseUri()
        val request = webClient.get()
            .uri { builder ->
                val filterName = when {
                    channelReference.startsWith("UC") -> "id"
                    channelReference.startsWith("@") -> "forHandle"
                    else -> "forHandle"
                }
                builder
                    .scheme(baseUri.scheme)
                    .host(baseUri.host)
                    .path("/youtube/v3/channels")
                    .queryParam("part", "snippet,contentDetails")
                    .queryParam(filterName, channelReference.removePrefix("@"))
                    .queryParam("key", appProperties.youtube.apiKey)
                    .build()
            }
            .retrieve()
            .bodyToMono(ChannelListResponse::class.java)
            .block()
            ?: throw BadRequestException("YouTube channel response is empty")

        val item = request.items.firstOrNull()
            ?: throw BadRequestException("YouTube channel '$channelReference' not found")
        return IdolYouTubeChannelSnapshot(
            resolvedChannelId = item.id,
            title = item.snippet?.title ?: channelReference,
            description = item.snippet?.description,
            profileImageUrl = item.snippet?.thumbnails?.highestUrl(),
            uploadsPlaylistId = item.contentDetails?.relatedPlaylists?.uploads,
        )
    }

    override fun fetchRecentVideos(
        uploadsPlaylistId: String,
        maxResults: Int,
    ): List<IdolYouTubeVideoSnapshot> {
        requireApiKey()
        val baseUri = youtubeBaseUri()
        val playlistResponse = webClient.get()
            .uri { builder ->
                builder
                    .scheme(baseUri.scheme)
                    .host(baseUri.host)
                    .path("/youtube/v3/playlistItems")
                    .queryParam("part", "snippet,contentDetails")
                    .queryParam("playlistId", uploadsPlaylistId)
                    .queryParam("maxResults", maxResults.coerceAtMost(50))
                    .queryParam("key", appProperties.youtube.apiKey)
                    .build()
            }
            .retrieve()
            .bodyToMono(PlaylistItemsResponse::class.java)
            .block()
            ?: throw BadRequestException("YouTube playlist response is empty")

        val videoIds = playlistResponse.items.mapNotNull { it.contentDetails?.videoId ?: it.snippet?.resourceId?.videoId }
        if (videoIds.isEmpty()) {
            return emptyList()
        }

        val videoDetails = webClient.get()
            .uri { builder ->
                builder
                    .scheme(baseUri.scheme)
                    .host(baseUri.host)
                    .path("/youtube/v3/videos")
                    .queryParam("part", "snippet,contentDetails")
                    .queryParam("id", videoIds.joinToString(","))
                    .queryParam("key", appProperties.youtube.apiKey)
                    .build()
            }
            .retrieve()
            .bodyToMono(VideosListResponse::class.java)
            .block()
            ?: throw BadRequestException("YouTube videos response is empty")

        val detailMap = videoDetails.items.associateBy { it.id }

        return playlistResponse.items.mapNotNull { item ->
            val videoId = item.contentDetails?.videoId ?: item.snippet?.resourceId?.videoId ?: return@mapNotNull null
            val detail = detailMap[videoId]
            val publishedAt = detail?.snippet?.publishedAt ?: item.contentDetails?.videoPublishedAt ?: item.snippet?.publishedAt ?: return@mapNotNull null
            val title = detail?.snippet?.title ?: item.snippet?.title ?: videoId
            val description = detail?.snippet?.description ?: item.snippet?.description
            val thumbnailUrl = detail?.snippet?.thumbnails?.highestUrl() ?: item.snippet?.thumbnails?.highestUrl()
            val durationText = detail?.contentDetails?.duration
            IdolYouTubeVideoSnapshot(
                externalVideoId = videoId,
                title = title,
                description = description,
                thumbnailUrl = thumbnailUrl,
                publishedAt = publishedAt,
                videoUrl = "https://youtube.com/watch?v=$videoId",
                contentType = classifyContentType(title, description, durationText),
            )
        }
    }

    private fun requireApiKey() {
        if (appProperties.youtube.apiKey.isBlank()) {
            throw BadRequestException("YOUTUBE_API_KEY is required for real YouTube sync")
        }
    }

    private fun youtubeBaseUri(): URI =
        runCatching { URI(appProperties.youtube.apiBaseUrl) }
            .getOrElse { throw BadRequestException("YOUTUBE_API_BASE_URL is invalid") }
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class VideosListResponse(
    val items: List<VideoItem> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class VideoItem(
    val id: String,
    val snippet: VideoSnippet? = null,
    val contentDetails: VideoContentDetails? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class VideoSnippet(
    val title: String? = null,
    val description: String? = null,
    val publishedAt: OffsetDateTime? = null,
    val thumbnails: ThumbnailBundle? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class VideoContentDetails(
    val duration: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class ChannelListResponse(
    val items: List<ChannelItem> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class ChannelItem(
    val id: String,
    val snippet: ChannelSnippet? = null,
    val contentDetails: ChannelContentDetails? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class ChannelSnippet(
    val title: String? = null,
    val description: String? = null,
    val thumbnails: ThumbnailBundle? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class ChannelContentDetails(
    val relatedPlaylists: RelatedPlaylists? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class RelatedPlaylists(
    val uploads: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class PlaylistItemsResponse(
    val items: List<PlaylistItem> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class PlaylistItem(
    val snippet: PlaylistSnippet? = null,
    val contentDetails: PlaylistContentDetails? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class PlaylistSnippet(
    val title: String? = null,
    val description: String? = null,
    val publishedAt: OffsetDateTime? = null,
    val thumbnails: ThumbnailBundle? = null,
    val resourceId: ResourceId? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class PlaylistContentDetails(
    val videoId: String? = null,
    val videoPublishedAt: OffsetDateTime? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class ResourceId(
    val videoId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class ThumbnailBundle(
    val maxres: ThumbnailValue? = null,
    val standard: ThumbnailValue? = null,
    val high: ThumbnailValue? = null,
    val medium: ThumbnailValue? = null,
    val default: ThumbnailValue? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class ThumbnailValue(
    val url: String? = null,
)

private fun ThumbnailBundle.highestUrl(): String? =
    maxres?.url ?: standard?.url ?: high?.url ?: medium?.url ?: default?.url

private fun classifyContentType(
    title: String,
    description: String?,
    durationText: String?,
): String {
    val normalizedText = "${title.lowercase()} ${(description ?: "").lowercase()}"
    if (normalizedText.contains("#shorts") || normalizedText.contains("shorts")) {
        return "short"
    }

    val duration = durationText?.let {
        runCatching { Duration.parse(it) }.getOrNull()
    }
    return if (duration != null && duration.seconds <= 60) "short" else "video"
}
