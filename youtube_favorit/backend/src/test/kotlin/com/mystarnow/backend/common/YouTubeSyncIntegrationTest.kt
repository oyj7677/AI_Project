package com.mystarnow.backend.common

import com.mystarnow.backend.persistence.readmodel.InfluencerServingStateProjector
import com.mystarnow.backend.persistence.repository.ActivityItemRepository
import com.mystarnow.backend.persistence.repository.ChannelRepository
import com.mystarnow.backend.persistence.repository.InfluencerRepository
import com.mystarnow.backend.persistence.repository.LiveStatusCacheRepository
import com.mystarnow.backend.persistence.repository.PlatformSyncMetadataRepository
import com.mystarnow.backend.persistence.repository.RawSourceRecordRepository
import com.mystarnow.backend.platform.integration.youtube.YouTubeActivitySnapshot
import com.mystarnow.backend.platform.integration.youtube.YouTubeAdapter
import com.mystarnow.backend.platform.integration.youtube.YouTubeLiveStatusSnapshot
import com.mystarnow.backend.platform.sync.YouTubeSyncService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.mockito.BDDMockito.given
import java.time.OffsetDateTime

class YouTubeSyncIntegrationTest : PostgresContainerTestBase() {
    @Autowired
    private lateinit var influencerRepository: InfluencerRepository

    @Autowired
    private lateinit var channelRepository: ChannelRepository

    @Autowired
    private lateinit var activityItemRepository: ActivityItemRepository

    @Autowired
    private lateinit var liveStatusCacheRepository: LiveStatusCacheRepository

    @Autowired
    private lateinit var rawSourceRecordRepository: RawSourceRecordRepository

    @Autowired
    private lateinit var platformSyncMetadataRepository: PlatformSyncMetadataRepository

    @Autowired
    private lateinit var projector: InfluencerServingStateProjector

    @Autowired
    private lateinit var youTubeSyncService: YouTubeSyncService

    @MockBean
    private lateinit var youTubeAdapter: YouTubeAdapter

    @Test
    fun `successful youtube sync writes raw normalized and sync metadata`() {
        val influencer = influencerRepository.save(TestDataFactory.influencer("haru", "하루"))
        val youtubeChannel = channelRepository.save(TestDataFactory.youtubeChannel(influencer))
        projector.refreshInfluencer(influencer.id)

        given(youTubeAdapter.fetchRecentActivities(youtubeChannel.externalChannelId, 20)).willReturn(listOf(
            YouTubeActivitySnapshot(
                contentType = "video",
                title = "Synced video",
                summary = "from adapter",
                thumbnailUrl = null,
                publishedAt = OffsetDateTime.parse("2026-04-04T11:00:00Z"),
                externalUrl = "https://youtube.com/watch?v=sync1",
            ),
        ))
        given(youTubeAdapter.fetchLiveStatus(youtubeChannel.externalChannelId)).willReturn(YouTubeLiveStatusSnapshot(
            isLive = true,
            liveTitle = "Live now",
            watchUrl = "https://youtube.com/watch?v=live1",
            viewerCount = 100,
            startedAt = OffsetDateTime.parse("2026-04-04T11:30:00Z"),
        ))
        given(youTubeAdapter.fetchChannelSnapshot(youtubeChannel.externalChannelId)).willReturn(
            com.mystarnow.backend.platform.integration.youtube.YouTubeChannelSnapshot(
                externalChannelId = youtubeChannel.externalChannelId,
                title = "Updated Channel",
                description = "updated bio",
                profileImageUrl = "https://cdn.test/profile.png",
            ),
        )

        youTubeSyncService.syncDueChannelProfiles()
        youTubeSyncService.syncDueActivities()
        youTubeSyncService.syncDueLiveStatus()

        assertEquals(1, activityItemRepository.findAllByChannelIdAndPlatformCodeAndSourceTypeOrderByPublishedAtDesc(youtubeChannel.id, "youtube", "imported").size)
        assertTrue(liveStatusCacheRepository.findById(youtubeChannel.id).orElseThrow().live)
        assertTrue(rawSourceRecordRepository.findAll().isNotEmpty())
        assertEquals(3, platformSyncMetadataRepository.findAll().size)
        assertTrue(platformSyncMetadataRepository.findAll().all { it.lastStatus == "success" })
    }

    @Test
    fun `failed youtube live sync marks stale cache and backoff`() {
        val influencer = influencerRepository.save(TestDataFactory.influencer("haru", "하루"))
        val youtubeChannel = channelRepository.save(TestDataFactory.youtubeChannel(influencer))
        liveStatusCacheRepository.save(TestDataFactory.liveStatus(youtubeChannel.id, influencer.id, isLive = true))
        projector.refreshInfluencer(influencer.id)

        given(youTubeAdapter.fetchLiveStatus(youtubeChannel.externalChannelId)).willThrow(IllegalStateException("upstream timeout"))

        youTubeSyncService.syncDueLiveStatus()

        val live = liveStatusCacheRepository.findById(youtubeChannel.id).orElseThrow()
        assertEquals("stale", live.freshnessStatus)
        assertEquals("failed", platformSyncMetadataRepository.findAll().first().lastStatus)
        assertTrue(platformSyncMetadataRepository.findAll().first().consecutiveFailures >= 1)
    }
}
