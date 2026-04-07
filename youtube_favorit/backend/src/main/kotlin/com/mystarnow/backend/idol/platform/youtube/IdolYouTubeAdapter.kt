package com.mystarnow.backend.idol.platform.youtube

import java.time.OffsetDateTime

interface IdolYouTubeAdapter {
    fun fetchChannelProfile(channelReference: String): IdolYouTubeChannelSnapshot

    fun fetchRecentVideos(uploadsPlaylistId: String, maxResults: Int): List<IdolYouTubeVideoSnapshot>
}

data class IdolYouTubeChannelSnapshot(
    val resolvedChannelId: String,
    val title: String,
    val description: String?,
    val profileImageUrl: String?,
    val uploadsPlaylistId: String?,
)

data class IdolYouTubeVideoSnapshot(
    val externalVideoId: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val publishedAt: OffsetDateTime,
    val videoUrl: String,
    val contentType: String,
)
