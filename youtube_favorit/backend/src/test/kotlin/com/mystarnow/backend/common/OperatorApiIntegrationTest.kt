package com.mystarnow.backend.common

import com.mystarnow.backend.persistence.repository.IdolGroupRepository
import com.mystarnow.backend.persistence.repository.IdolMemberRepository
import com.mystarnow.backend.persistence.repository.YouTubeChannelRepository
import com.mystarnow.backend.persistence.repository.YouTubeVideoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
class OperatorApiIntegrationTest : PostgresContainerTestBase() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var groupRepository: IdolGroupRepository

    @Autowired
    private lateinit var memberRepository: IdolMemberRepository

    @Autowired
    private lateinit var channelRepository: YouTubeChannelRepository

    @Autowired
    private lateinit var videoRepository: YouTubeVideoRepository

    @Test
    fun `operator can onboard a group member channel and video`() {
        mockMvc.perform(
            post("/internal/operator/groups")
                .with(httpBasic("operator", "operator"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "slug":"starwave",
                      "displayName":"StarWave",
                      "description":"5인조 그룹",
                      "featured":true
                    }
                    """.trimIndent(),
                ),
        ).andExpect(status().isOk)

        val group = groupRepository.findBySlugAndDeletedAtIsNull("starwave")
            ?: error("group not created")

        mockMvc.perform(
            post("/internal/operator/members")
                .with(httpBasic("operator", "operator"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "groupId":"${group.id}",
                      "slug":"harin",
                      "displayName":"하린",
                      "sortOrder":1
                    }
                    """.trimIndent(),
                ),
        ).andExpect(status().isOk)

        val member = memberRepository.findBySlugAndDeletedAtIsNull("harin")
            ?: error("member not created")

        mockMvc.perform(
            post("/internal/operator/channels")
                .with(httpBasic("operator", "operator"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "platformCode":"youtube",
                      "externalChannelId":"UC-harin-log",
                      "handle":"@harinlog",
                      "channelUrl":"https://youtube.com/@harinlog",
                      "displayLabel":"하린 로그",
                      "channelType":"MEMBER_PERSONAL",
                      "ownerType":"MEMBER",
                      "ownerMemberId":"${member.id}",
                      "isOfficial":true,
                      "isPrimary":true
                    }
                    """.trimIndent(),
                ),
        ).andExpect(status().isOk)

        val channel = channelRepository.findByPlatformCodeAndExternalChannelIdAndDeletedAtIsNull("youtube", "UC-harin-log")
            ?: error("channel not created")

        mockMvc.perform(
            post("/internal/operator/videos")
                .with(httpBasic("operator", "operator"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "channelId":"${channel.id}",
                      "externalVideoId":"harin-vlog-1",
                      "title":"하린 브이로그",
                      "description":"연습실 브이로그",
                      "publishedAt":"2026-04-05T08:30:00Z",
                      "videoUrl":"https://youtube.com/watch?v=harin-vlog-1",
                      "pinned":true
                    }
                    """.trimIndent(),
                ),
        ).andExpect(status().isOk)

        assertEquals(1, memberRepository.findAllByGroupIdAndStatusAndDeletedAtIsNullOrderBySortOrderAsc(group.id, "active").size)
        assertEquals(1, channelRepository.findAllByOwnerMemberIdAndDeletedAtIsNull(member.id).size)
        assertEquals(1, videoRepository.findAllByChannelIdAndDeletedAtIsNullOrderByPublishedAtDesc(channel.id).size)

        mockMvc.perform(get("/v1/groups/{groupSlug}", "starwave"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.members.data.items[0].memberSlug").value("harin"))
            .andExpect(jsonPath("$.data.recentVideos.data.items[0].title").value("하린 브이로그"))

        mockMvc.perform(get("/v1/members/{memberSlug}", "harin"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.personalChannels.data.items[0].channelName").value("하린 로그"))
            .andExpect(jsonPath("$.data.recentVideos.data.items[0].title").value("하린 브이로그"))

        mockMvc.perform(get("/v1/home").param("limit", "4"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.recentVideos.data.items[0].group.groupSlug").value("starwave"))
            .andExpect(jsonPath("$.data.recentVideos.data.items[0].member.memberSlug").value("harin"))
    }

    @Test
    fun `operator can update group and member metadata`() {
        mockMvc.perform(
            post("/internal/operator/groups")
                .with(httpBasic("operator", "operator"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"slug":"moonlight","displayName":"MoonLight"}"""),
        ).andExpect(status().isOk)
        val group = groupRepository.findBySlugAndDeletedAtIsNull("moonlight") ?: error("group missing")

        mockMvc.perform(
            post("/internal/operator/members")
                .with(httpBasic("operator", "operator"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"groupId":"${group.id}","slug":"yuna","displayName":"유나"}"""),
        ).andExpect(status().isOk)

        mockMvc.perform(
            put("/internal/operator/groups/{groupSlug}", "moonlight")
                .with(httpBasic("operator", "operator"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"description":"밴드형 퍼포먼스 그룹","featured":true}"""),
        ).andExpect(status().isOk)

        mockMvc.perform(
            put("/internal/operator/members/{memberSlug}", "yuna")
                .with(httpBasic("operator", "operator"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"profileImageUrl":"https://cdn.test/yuna.jpg","sortOrder":3}"""),
        ).andExpect(status().isOk)

        val updatedGroup = groupRepository.findBySlugAndDeletedAtIsNull("moonlight") ?: error("updated group missing")
        val updatedMember = memberRepository.findBySlugAndDeletedAtIsNull("yuna") ?: error("updated member missing")

        assertTrue(updatedGroup.featured)
        assertEquals("밴드형 퍼포먼스 그룹", updatedGroup.description)
        assertEquals(3, updatedMember.sortOrder)
        assertEquals("https://cdn.test/yuna.jpg", updatedMember.profileImageUrl)
    }
}
