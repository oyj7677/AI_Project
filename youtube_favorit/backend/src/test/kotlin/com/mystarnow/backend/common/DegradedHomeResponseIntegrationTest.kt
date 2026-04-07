package com.mystarnow.backend.common

import com.mystarnow.backend.persistence.readmodel.InfluencerServingStateProjector
import com.mystarnow.backend.persistence.repository.ActivityItemRepository
import com.mystarnow.backend.persistence.repository.ChannelRepository
import com.mystarnow.backend.persistence.repository.InfluencerCategoryRepository
import com.mystarnow.backend.persistence.repository.InfluencerRepository
import com.mystarnow.backend.persistence.repository.PlatformSyncMetadataRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime

@AutoConfigureMockMvc
class DegradedHomeResponseIntegrationTest : PostgresContainerTestBase() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var influencerRepository: InfluencerRepository

    @Autowired
    private lateinit var influencerCategoryRepository: InfluencerCategoryRepository

    @Autowired
    private lateinit var channelRepository: ChannelRepository

    @Autowired
    private lateinit var activityItemRepository: ActivityItemRepository

    @Autowired
    private lateinit var syncMetadataRepository: PlatformSyncMetadataRepository

    @Autowired
    private lateinit var projector: InfluencerServingStateProjector

    @Test
    fun `home latest updates returns partial when youtube is degraded but instagram manual data exists`() {
        val influencer = TestDataFactory.influencer("haru", "하루", featured = true)
        influencerRepository.save(influencer)
        influencerCategoryRepository.save(TestDataFactory.categoryLink(influencer.id, "game"))
        val youtubeChannel = channelRepository.save(TestDataFactory.youtubeChannel(influencer))
        val instagramChannel = channelRepository.save(TestDataFactory.instagramChannel(influencer))
        activityItemRepository.save(
            TestDataFactory.activity(
                influencerId = influencer.id,
                channelId = instagramChannel.id,
                platform = "instagram",
                sourceType = "manual",
                title = "인스타그램 수동 공지",
                publishedAt = OffsetDateTime.parse("2026-04-04T11:30:00Z"),
            ),
        )
        syncMetadataRepository.save(
            TestDataFactory.syncMetadata(
                channelId = youtubeChannel.id,
                influencerId = influencer.id,
                scope = "channel_activity",
                status = "failed",
                consecutiveFailures = 3,
                errorCode = "YOUTUBE_QUOTA_EXCEEDED",
                errorMessage = "YouTube activity sync failed",
                backoffUntil = OffsetDateTime.parse("2026-04-04T12:15:00Z"),
            ),
        )
        projector.refreshInfluencer(influencer.id)

        mockMvc.perform(get("/v1/home"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.meta.partialFailure").value(true))
            .andExpect(jsonPath("$.data.latestUpdates.status").value("partial"))
            .andExpect(jsonPath("$.data.latestUpdates.data.items[0].platform").value("instagram"))
            .andExpect(jsonPath("$.errors[0].section").value("latestUpdates"))
    }
}

