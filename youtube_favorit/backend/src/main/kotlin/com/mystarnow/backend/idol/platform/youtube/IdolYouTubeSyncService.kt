package com.mystarnow.backend.idol.platform.youtube

import com.mystarnow.backend.common.api.FreshnessStatus
import com.mystarnow.backend.persistence.entity.YouTubeVideoEntity
import com.mystarnow.backend.persistence.repository.YouTubeChannelRepository
import com.mystarnow.backend.persistence.repository.YouTubeVideoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

@Service
class IdolYouTubeSyncService(
    private val adapter: IdolYouTubeAdapter,
    private val channelRepository: YouTubeChannelRepository,
    private val videoRepository: YouTubeVideoRepository,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun syncChannel(channelId: UUID): Int {
        val channel = channelRepository.findById(channelId).orElse(null) ?: return 0
        if (channel.platformCode != "youtube" || channel.status != "active") {
            return 0
        }

        val profile = adapter.fetchChannelProfile(channel.externalChannelId)
        channel.externalChannelId = profile.resolvedChannelId
        channel.displayLabel = profile.title
        channel.uploadsPlaylistId = profile.uploadsPlaylistId
        channel.lastSeenAt = OffsetDateTime.now(clock)
        channelRepository.save(channel)

        val uploadsPlaylistId = profile.uploadsPlaylistId ?: return 0
        val snapshots = adapter.fetchRecentVideos(uploadsPlaylistId, 20)
        val importedIds = mutableSetOf<String>()
        snapshots.forEach { snapshot ->
            importedIds += snapshot.externalVideoId
            val existing = videoRepository.findByExternalVideoIdAndDeletedAtIsNull(snapshot.externalVideoId)
            if (existing != null) {
                existing.channelId = channel.id
                existing.title = snapshot.title
                existing.description = snapshot.description
                existing.thumbnailUrl = snapshot.thumbnailUrl
                existing.publishedAt = snapshot.publishedAt
                existing.videoUrl = snapshot.videoUrl
                existing.contentType = snapshot.contentType
                existing.sourceType = "youtube_imported"
                existing.freshnessStatus = FreshnessStatus.fresh.name.lowercase()
                existing.lastSuccessfulSyncAt = OffsetDateTime.now(clock)
                existing.deletedAt = null
                videoRepository.save(existing)
            } else {
                videoRepository.save(
                    YouTubeVideoEntity(
                        id = UUID.randomUUID(),
                        channelId = channel.id,
                        externalVideoId = snapshot.externalVideoId,
                        title = snapshot.title,
                        description = snapshot.description,
                        thumbnailUrl = snapshot.thumbnailUrl,
                        publishedAt = snapshot.publishedAt,
                        videoUrl = snapshot.videoUrl,
                        contentType = snapshot.contentType,
                        sourceType = "youtube_imported",
                        freshnessStatus = FreshnessStatus.fresh.name.lowercase(),
                        lastSuccessfulSyncAt = OffsetDateTime.now(clock),
                    ),
                )
            }
        }

        val staleImportedVideos = videoRepository.findAllByChannelIdAndDeletedAtIsNullOrderByPublishedAtDesc(channel.id)
            .filter { it.sourceType == "youtube_imported" && it.externalVideoId !in importedIds }
        staleImportedVideos.forEach {
            it.deletedAt = OffsetDateTime.now(clock)
            videoRepository.save(it)
        }

        log.info("idol youtube sync completed channelId={} fetchedVideos={}", channel.id, snapshots.size)
        return snapshots.size
    }

    @Transactional
    fun syncAllActiveChannels(): Int =
        channelRepository.findAllByPlatformCodeAndStatusAndDeletedAtIsNull("youtube", "active")
            .sumOf { syncChannel(it.id) }
}
