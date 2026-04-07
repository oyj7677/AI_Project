package com.mystarnow.backend.platform.integration.youtube

import java.time.OffsetDateTime

interface YouTubeAdapter {
    fun fetchChannelSnapshot(externalChannelId: String): YouTubeChannelSnapshot

    fun fetchRecentActivities(externalChannelId: String, maxResults: Int): List<YouTubeActivitySnapshot>

    fun fetchLiveStatus(externalChannelId: String): YouTubeLiveStatusSnapshot
}

data class YouTubeChannelSnapshot(
    val externalChannelId: String,
    val title: String,
    val description: String?,
    val profileImageUrl: String?,
)

data class YouTubeActivitySnapshot(
    val contentType: String,
    val title: String,
    val summary: String?,
    val thumbnailUrl: String?,
    val publishedAt: OffsetDateTime,
    val externalUrl: String,
)

data class YouTubeLiveStatusSnapshot(
    val isLive: Boolean,
    val liveTitle: String?,
    val watchUrl: String?,
    val viewerCount: Int?,
    val startedAt: OffsetDateTime?,
)

