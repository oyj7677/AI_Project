package com.mystarnow.backend.home

import com.mystarnow.backend.home.service.HomeQueryService
import com.mystarnow.backend.idol.service.GroupAggregate
import com.mystarnow.backend.idol.service.IdolCatalogReadSupport
import com.mystarnow.backend.persistence.entity.IdolGroupEntity
import com.mystarnow.backend.persistence.entity.IdolMemberEntity
import com.mystarnow.backend.persistence.entity.YouTubeChannelEntity
import com.mystarnow.backend.persistence.entity.YouTubeVideoEntity
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class HomeQueryServiceTest {
    private val clock = Clock.fixed(Instant.parse("2026-04-05T10:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `builds paged recent video feed and featured groups`() {
        val readSupport = mockk<IdolCatalogReadSupport>()
        every { readSupport.loadActiveGroupAggregates() } returns aggregates()

        val service = HomeQueryService(
            readSupport = readSupport,
            clock = clock,
        )

        val response = service.getHome(
            requestId = "req-home",
            cursor = null,
            limit = 2,
            contentType = null,
        )
        val data = requireNotNull(response.data)

        assertEquals(2, data.recentVideos.data.items.size)
        assertTrue(data.recentVideos.data.pageInfo.hasNext)
        assertEquals(2, data.featuredGroups.data.items.size)
        assertEquals("starwave", data.recentVideos.data.items.first().group.groupSlug)
    }

    private fun aggregates(): List<GroupAggregate> {
        val now = OffsetDateTime.now(clock)
        val starwave = IdolGroupEntity(
            id = namedUuid("group-starwave"),
            slug = "starwave",
            displayName = "StarWave",
            normalizedName = "starwave",
            featured = true,
            status = "active",
        )
        val moonlight = IdolGroupEntity(
            id = namedUuid("group-moonlight"),
            slug = "moonlight",
            displayName = "MoonLight",
            normalizedName = "moonlight",
            featured = true,
            status = "active",
        )
        val harin = IdolMemberEntity(
            id = namedUuid("member-harin"),
            groupId = starwave.id,
            slug = "harin",
            displayName = "하린",
            normalizedName = "하린",
        )
        val official = YouTubeChannelEntity(
            id = namedUuid("channel-starwave"),
            externalChannelId = "UC-starwave",
            channelUrl = "https://youtube.com/@starwave",
            displayLabel = "StarWave Official",
            channelType = "GROUP_OFFICIAL",
            ownerType = "GROUP",
            ownerGroupId = starwave.id,
            official = true,
            primary = true,
        )
        val harinChannel = YouTubeChannelEntity(
            id = namedUuid("channel-harin"),
            externalChannelId = "UC-harin",
            channelUrl = "https://youtube.com/@harin",
            displayLabel = "하린 로그",
            channelType = "MEMBER_PERSONAL",
            ownerType = "MEMBER",
            ownerMemberId = harin.id,
            official = true,
            primary = true,
        )
        val moonlightOfficial = YouTubeChannelEntity(
            id = namedUuid("channel-moonlight"),
            externalChannelId = "UC-moonlight",
            channelUrl = "https://youtube.com/@moonlight",
            displayLabel = "MoonLight Official",
            channelType = "GROUP_OFFICIAL",
            ownerType = "GROUP",
            ownerGroupId = moonlight.id,
            official = true,
            primary = true,
        )
        return listOf(
            GroupAggregate(
                group = starwave,
                members = listOf(harin),
                officialChannels = listOf(official),
                memberChannels = listOf(harinChannel),
                videos = listOf(
                    YouTubeVideoEntity(
                        id = UUID.randomUUID(),
                        channelId = official.id,
                        externalVideoId = "video-1",
                        title = "공식 영상",
                        publishedAt = now.minusHours(1),
                        videoUrl = "https://youtube.com/watch?v=video-1",
                        sourceType = "youtube_imported",
                        freshnessStatus = "fresh",
                    ),
                    YouTubeVideoEntity(
                        id = UUID.randomUUID(),
                        channelId = harinChannel.id,
                        externalVideoId = "video-2",
                        title = "하린 브이로그",
                        publishedAt = now.minusHours(2),
                        videoUrl = "https://youtube.com/watch?v=video-2",
                        sourceType = "youtube_imported",
                        freshnessStatus = "fresh",
                    ),
                ),
            ),
            GroupAggregate(
                group = moonlight,
                members = emptyList(),
                officialChannels = listOf(moonlightOfficial),
                memberChannels = emptyList(),
                videos = listOf(
                    YouTubeVideoEntity(
                        id = UUID.randomUUID(),
                        channelId = moonlightOfficial.id,
                        externalVideoId = "video-3",
                        title = "밴드 세션",
                        publishedAt = now.minusHours(3),
                        videoUrl = "https://youtube.com/watch?v=video-3",
                        sourceType = "youtube_imported",
                        freshnessStatus = "fresh",
                    ),
                ),
            ),
        )
    }

    private fun namedUuid(seed: String): UUID =
        UUID.nameUUIDFromBytes(seed.toByteArray(StandardCharsets.UTF_8))
}
