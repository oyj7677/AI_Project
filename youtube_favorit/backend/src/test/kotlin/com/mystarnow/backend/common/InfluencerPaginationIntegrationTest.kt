package com.mystarnow.backend.common

import com.mystarnow.backend.persistence.readmodel.InfluencerServingStateProjector
import com.mystarnow.backend.persistence.repository.ChannelRepository
import com.mystarnow.backend.persistence.repository.InfluencerCategoryRepository
import com.mystarnow.backend.persistence.repository.InfluencerRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
class InfluencerPaginationIntegrationTest : PostgresContainerTestBase() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var influencerRepository: InfluencerRepository

    @Autowired
    private lateinit var influencerCategoryRepository: InfluencerCategoryRepository

    @Autowired
    private lateinit var channelRepository: ChannelRepository

    @Autowired
    private lateinit var projector: InfluencerServingStateProjector

    @Test
    fun `real pagination remains stable across cursor pages`() {
        (1..5).forEach { index ->
            val influencer = TestDataFactory.influencer("creator-$index", "크리에이터 $index", featured = index <= 2)
            influencerRepository.save(influencer)
            influencerCategoryRepository.save(TestDataFactory.categoryLink(influencer.id, if (index % 2 == 0) "talk" else "game"))
            channelRepository.save(TestDataFactory.youtubeChannel(influencer))
            projector.refreshInfluencer(influencer.id)
        }

        val first = mockMvc.perform(get("/v1/influencers").queryParam("platform", "youtube").queryParam("limit", "2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.results.data.items.length()").value(2))
            .andExpect(jsonPath("$.data.results.data.pageInfo.hasNext").value(true))
            .andReturn()

        val firstCursor = com.jayway.jsonpath.JsonPath.read<String>(
            first.response.contentAsString,
            "$.data.results.data.pageInfo.nextCursor",
        )

        mockMvc.perform(
            get("/v1/influencers")
                .queryParam("platform", "youtube")
                .queryParam("limit", "2")
                .queryParam("cursor", firstCursor),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.results.data.items.length()").value(2))
    }
}

