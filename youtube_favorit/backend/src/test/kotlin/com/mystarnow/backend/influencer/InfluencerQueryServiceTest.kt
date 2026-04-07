package com.mystarnow.backend.influencer

import com.mystarnow.backend.common.config.AppProperties
import com.mystarnow.backend.common.config.PlatformSupportEntry
import com.mystarnow.backend.common.config.PlatformSupportProperties
import com.mystarnow.backend.influencer.service.InfluencerQueryService
import com.mystarnow.backend.persistence.readmodel.ActivityRecord
import com.mystarnow.backend.persistence.readmodel.CategoryRecord
import com.mystarnow.backend.persistence.readmodel.ChannelRecord
import com.mystarnow.backend.persistence.readmodel.InfluencerAggregate
import com.mystarnow.backend.persistence.readmodel.InfluencerReadModelRepository
import com.mystarnow.backend.persistence.readmodel.LiveStatusRecord
import com.mystarnow.backend.persistence.readmodel.ScheduleRecord
import com.mystarnow.backend.persistence.repository.ActivityItemRepository
import com.mystarnow.backend.persistence.repository.LiveStatusCacheRepository
import com.mystarnow.backend.persistence.repository.PlatformSyncMetadataRepository
import com.mystarnow.backend.platform.sync.SectionDegradationService
import com.mystarnow.backend.platform.sync.SyncBackoffPolicy
import com.mystarnow.backend.platform.sync.SyncMetricsService
import io.mockk.every
import io.mockk.mockk
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class InfluencerQueryServiceTest {
    private val fixedClock = Clock.fixed(Instant.parse("2026-04-04T12:00:00Z"), ZoneOffset.UTC)
    private val appProperties = AppProperties(
        baseUrl = "http://localhost:8080",
        timezone = "Asia/Seoul",
        defaultPageSize = 20,
        maxPageSize = 50,
        support = PlatformSupportProperties(
            platforms = listOf(
                PlatformSupportEntry("youtube", true, "auto"),
                PlatformSupportEntry("instagram", true, "limited"),
                PlatformSupportEntry("x", false, "disabled"),
            ),
        ),
    )
    private val degradationService = sectionDegradationService()

    @Test
    fun `filters influencers by category and paginates with cursor`() {
        val service = InfluencerQueryService(
            readModelRepository = FakeReadModelRepository(sampleInfluencers(6)),
            appProperties = appProperties,
            sectionDegradationService = degradationService,
            clock = fixedClock,
        )

        val first = service.getInfluencers(
            requestId = "req-1",
            q = null,
            category = "game",
            platforms = listOf("youtube"),
            sort = "featured",
            cursor = null,
            limit = 2,
        )
        val firstData = requireNotNull(first.data)

        assertEquals(2, firstData.results.data.items.size)
        assertTrue(firstData.results.data.pageInfo.hasNext)
        assertNotNull(firstData.results.data.pageInfo.nextCursor)

        val second = service.getInfluencers(
            requestId = "req-2",
            q = null,
            category = "game",
            platforms = listOf("youtube"),
            sort = "featured",
            cursor = firstData.results.data.pageInfo.nextCursor,
            limit = 2,
        )
        val secondData = requireNotNull(second.data)

        assertEquals(1, secondData.results.data.items.size)
        assertFalse(secondData.results.data.pageInfo.hasNext)
    }

    @Test
    fun `searches by name and returns detail`() {
        val repository = FakeReadModelRepository(sampleInfluencers(3))
        val service = InfluencerQueryService(repository, appProperties, degradationService, fixedClock)

        val list = service.getInfluencers(
            requestId = "req-3",
            q = "하루",
            category = null,
            platforms = emptyList(),
            sort = "name",
            cursor = null,
            limit = 20,
        )
        val listData = requireNotNull(list.data)

        assertEquals(1, listData.results.data.items.size)
        assertEquals("haru", listData.results.data.items.first().slug)

        val detail = service.getInfluencerDetail(
            requestId = "req-4",
            slug = "haru",
            timezone = "Asia/Seoul",
            activitiesLimit = 5,
            schedulesLimit = 5,
        )
        val detailData = requireNotNull(detail.data)

        assertEquals("haru", detailData.profile.data.slug)
        assertEquals(2, detailData.channels.data.items.size)
        assertTrue(detailData.liveStatus.data.isLive)
    }

    @Test
    fun `throws on invalid cursor`() {
        val service = InfluencerQueryService(
            readModelRepository = FakeReadModelRepository(sampleInfluencers(2)),
            appProperties = appProperties,
            sectionDegradationService = degradationService,
            clock = fixedClock,
        )

        assertThrows(RuntimeException::class.java) {
            service.getInfluencers(
                requestId = "req-5",
                q = null,
                category = null,
                platforms = emptyList(),
                sort = "featured",
                cursor = "%%%invalid",
                limit = 20,
            )
        }
    }

    private fun sampleInfluencers(count: Int): List<InfluencerAggregate> {
        val now = OffsetDateTime.now(fixedClock)
        return (0 until count).map { index ->
            val slug = if (index == 0) "haru" else "creator-${index + 1}"
            val id = UUID.nameUUIDFromBytes(slug.toByteArray())
            InfluencerAggregate(
                id = id,
                slug = slug,
                displayName = if (index == 0) "하루" else "크리에이터 ${index + 1}",
                bio = "bio-$slug",
                profileImageUrl = null,
                featured = index < 3,
                latestActivityAt = now.minusHours(index.toLong()),
                currentLivePlatform = if (index == 0) "youtube" else null,
                liveNow = index == 0,
                categories = listOf(CategoryRecord(if (index % 2 == 0) "game" else "talk", "label")),
                channels = listOf(
                    ChannelRecord(UUID.randomUUID(), "youtube", "@$slug", "https://youtube.com/$slug", true, true, "yt-$slug"),
                    ChannelRecord(UUID.randomUUID(), "instagram", "@$slug.daily", "https://instagram.com/$slug", true, false, "ig-$slug"),
                ),
                liveStatuses = if (index == 0) {
                    listOf(
                        LiveStatusRecord(
                            channelId = UUID.randomUUID(),
                            influencerId = id,
                            platform = "youtube",
                            isLive = true,
                            liveTitle = "Live",
                            watchUrl = "https://youtube.com/watch?v=1",
                            viewerCount = 10,
                            startedAt = now.minusMinutes(10),
                            snapshotAt = now.minusMinutes(1),
                            freshnessStatus = "fresh",
                            staleAt = null,
                        ),
                    )
                } else {
                    emptyList()
                },
                recentActivities = listOf(
                    ActivityRecord(UUID.randomUUID(), id, "youtube", "video", "Video", null, null, now.minusHours(index.toLong()), "https://youtube.com", "fresh", null),
                ),
                schedules = listOf(
                    ScheduleRecord(UUID.randomUUID(), id, "youtube", "Schedule", null, now.plusDays(1)),
                ),
                servingState = null,
            )
        }
    }

    private class FakeReadModelRepository(
        private val influencers: List<InfluencerAggregate>,
    ) : InfluencerReadModelRepository {
        override fun loadActiveInfluencers(): List<InfluencerAggregate> = influencers

        override fun loadInfluencerBySlug(slug: String): InfluencerAggregate? = influencers.firstOrNull { it.slug == slug }

        override fun loadLatestActivities(limit: Int): List<ActivityRecord> =
            influencers.flatMap { it.recentActivities }.sortedByDescending { it.publishedAt }.take(limit)

        override fun loadSchedulesBetween(start: OffsetDateTime, end: OffsetDateTime): List<ScheduleRecord> =
            influencers.flatMap { it.schedules }.filter { !it.scheduledAt.isBefore(start) && it.scheduledAt.isBefore(end) }
    }

    private fun sectionDegradationService(): SectionDegradationService {
        val metadataRepository = mockk<PlatformSyncMetadataRepository>()
        val liveRepository = mockk<LiveStatusCacheRepository>()
        val activityRepository = mockk<ActivityItemRepository>()
        every { metadataRepository.findAllByPlatformCodeAndResourceScopeAndChannelIdIn(any(), any(), any()) } returns emptyList()
        every { liveRepository.count() } returns 0
        every { liveRepository.countByFreshnessStatus(any()) } returns 0
        every { activityRepository.count() } returns 0
        every { activityRepository.countByFreshnessStatus(any()) } returns 0
        return SectionDegradationService(
            repository = metadataRepository,
            clock = fixedClock,
            backoffPolicy = SyncBackoffPolicy(appProperties),
            syncMetricsService = SyncMetricsService(SimpleMeterRegistry(), liveRepository, activityRepository),
        )
    }
}
