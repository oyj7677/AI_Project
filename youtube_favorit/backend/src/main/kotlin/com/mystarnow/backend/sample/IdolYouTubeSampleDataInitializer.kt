package com.mystarnow.backend.sample

import com.mystarnow.backend.common.api.FreshnessStatus
import com.mystarnow.backend.common.config.AppProperties
import com.mystarnow.backend.persistence.entity.IdolGroupEntity
import com.mystarnow.backend.persistence.entity.IdolMemberEntity
import com.mystarnow.backend.persistence.entity.YouTubeChannelEntity
import com.mystarnow.backend.persistence.entity.YouTubeVideoEntity
import com.mystarnow.backend.persistence.repository.IdolGroupRepository
import com.mystarnow.backend.persistence.repository.IdolMemberRepository
import com.mystarnow.backend.persistence.repository.YouTubeChannelRepository
import com.mystarnow.backend.persistence.repository.YouTubeVideoRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

@Configuration
class IdolYouTubeSampleDataInitializer {
    @Bean
    @Profile("local", "dev", "integration")
    fun idolSampleDataRunner(
        appProperties: AppProperties,
        groupRepository: IdolGroupRepository,
        memberRepository: IdolMemberRepository,
        channelRepository: YouTubeChannelRepository,
        videoRepository: YouTubeVideoRepository,
        clock: Clock,
    ): CommandLineRunner = CommandLineRunner {
        if (!appProperties.sampleData.enabled || groupRepository.count() > 0L) {
            return@CommandLineRunner
        }

        val now = OffsetDateTime.now(clock)

        val itzy = IdolGroupEntity(
            id = namedUuid("idol-group-itzy"),
            slug = "itzy",
            displayName = "있지",
            normalizedName = "있지",
            description = "에너지 있는 퍼포먼스와 개성 강한 브이로그 무드가 강한 5인조 그룹",
            coverImageUrl = "https://cdn.mystarnow.dev/groups/itzy.jpg",
            featured = true,
            status = "active",
        )
        groupRepository.save(itzy)

        val members = listOf(
            IdolMemberEntity(
                id = namedUuid("idol-member-itzy-yeji"),
                groupId = itzy.id,
                slug = "yeji",
                displayName = "예지",
                normalizedName = "예지",
                profileImageUrl = "https://cdn.mystarnow.dev/members/yeji.jpg",
                sortOrder = 1,
            ),
            IdolMemberEntity(
                id = namedUuid("idol-member-itzy-lia"),
                groupId = itzy.id,
                slug = "lia",
                displayName = "리아",
                normalizedName = "리아",
                profileImageUrl = "https://cdn.mystarnow.dev/members/lia.jpg",
                sortOrder = 2,
            ),
            IdolMemberEntity(
                id = namedUuid("idol-member-itzy-ryujin"),
                groupId = itzy.id,
                slug = "ryujin",
                displayName = "류진",
                normalizedName = "류진",
                profileImageUrl = "https://cdn.mystarnow.dev/members/ryujin.jpg",
                sortOrder = 3,
            ),
            IdolMemberEntity(
                id = namedUuid("idol-member-itzy-chaeryeong"),
                groupId = itzy.id,
                slug = "chaeryeong",
                displayName = "채령",
                normalizedName = "채령",
                profileImageUrl = "https://cdn.mystarnow.dev/members/chaeryeong.jpg",
                sortOrder = 4,
            ),
            IdolMemberEntity(
                id = namedUuid("idol-member-itzy-yuna"),
                groupId = itzy.id,
                slug = "yuna",
                displayName = "유나",
                normalizedName = "유나",
                profileImageUrl = "https://cdn.mystarnow.dev/members/yuna.jpg",
                sortOrder = 5,
            ),
        )
        memberRepository.saveAll(members)

        val channels = listOf(
            YouTubeChannelEntity(
                id = namedUuid("idol-channel-itzy-official"),
                externalChannelId = "UC-itzy-official",
                handle = "@ITZY",
                channelUrl = "https://youtube.com/@ITZY",
                displayLabel = "ITZY",
                channelType = "GROUP_OFFICIAL",
                ownerType = "GROUP",
                ownerGroupId = itzy.id,
                official = true,
                primary = true,
            ),
            YouTubeChannelEntity(
                id = namedUuid("idol-channel-itzy-behind"),
                externalChannelId = "UC-itzy-behind",
                handle = "@ITZYbehind",
                channelUrl = "https://youtube.com/@ITZYbehind",
                displayLabel = "ITZY Behind",
                channelType = "LABEL",
                ownerType = "GROUP",
                ownerGroupId = itzy.id,
                official = true,
                primary = false,
            ),
            YouTubeChannelEntity(
                id = namedUuid("idol-channel-yeji-room"),
                externalChannelId = "UC-yeji-room",
                handle = "@yejiroom",
                channelUrl = "https://youtube.com/@yejiroom",
                displayLabel = "예지 룸",
                channelType = "MEMBER_PERSONAL",
                ownerType = "MEMBER",
                ownerMemberId = members[0].id,
                official = true,
                primary = true,
            ),
            YouTubeChannelEntity(
                id = namedUuid("idol-channel-ryujin-log"),
                externalChannelId = "UC-ryujin-log",
                handle = "@ryujinlog",
                channelUrl = "https://youtube.com/@ryujinlog",
                displayLabel = "류진 로그",
                channelType = "MEMBER_PERSONAL",
                ownerType = "MEMBER",
                ownerMemberId = members[2].id,
                official = true,
                primary = true,
            ),
            YouTubeChannelEntity(
                id = namedUuid("idol-channel-yuna-room"),
                externalChannelId = "UC-yuna-room",
                handle = "@yunaroom",
                channelUrl = "https://youtube.com/@yunaroom",
                displayLabel = "유나 룸",
                channelType = "MEMBER_PERSONAL",
                ownerType = "MEMBER",
                ownerMemberId = members[4].id,
                official = true,
                primary = true,
            ),
        )
        channelRepository.saveAll(channels)

        val videos = listOf(
            YouTubeVideoEntity(
                id = UUID.randomUUID(),
                channelId = channels[0].id,
                externalVideoId = "itzy-comeback-diary",
                title = "있지 컴백 다이어리",
                description = "컴백 준비 비하인드 영상",
                thumbnailUrl = "https://img.youtube.com/vi/itzy-comeback-diary/hqdefault.jpg",
                publishedAt = now.minusHours(2),
                videoUrl = "https://youtube.com/watch?v=itzy-comeback-diary",
                sourceType = "youtube_imported",
                freshnessStatus = FreshnessStatus.fresh.name.lowercase(),
            ),
            YouTubeVideoEntity(
                id = UUID.randomUUID(),
                channelId = channels[2].id,
                externalVideoId = "yeji-room-practice",
                title = "예지 룸: 안무 연습 기록",
                description = "연습실에서 찍은 짧은 브이로그",
                thumbnailUrl = "https://img.youtube.com/vi/yeji-room-practice/hqdefault.jpg",
                publishedAt = now.minusHours(5),
                videoUrl = "https://youtube.com/watch?v=yeji-room-practice",
                sourceType = "youtube_imported",
                freshnessStatus = FreshnessStatus.fresh.name.lowercase(),
            ),
            YouTubeVideoEntity(
                id = UUID.randomUUID(),
                channelId = channels[3].id,
                externalVideoId = "ryujin-log-studio",
                title = "류진 로그: 스튜디오 하루",
                description = "스튜디오 비하인드 브이로그",
                thumbnailUrl = "https://img.youtube.com/vi/ryujin-log-studio/hqdefault.jpg",
                publishedAt = now.minusHours(8),
                videoUrl = "https://youtube.com/watch?v=ryujin-log-studio",
                sourceType = "youtube_imported",
                freshnessStatus = FreshnessStatus.fresh.name.lowercase(),
            ),
            YouTubeVideoEntity(
                id = UUID.randomUUID(),
                channelId = channels[4].id,
                externalVideoId = "yuna-room-cover",
                title = "유나 룸: 커버 연습 영상",
                description = "보컬 커버 연습 클립",
                thumbnailUrl = "https://img.youtube.com/vi/yuna-room-cover/hqdefault.jpg",
                publishedAt = now.minusHours(12),
                videoUrl = "https://youtube.com/watch?v=yuna-room-cover",
                sourceType = "manual",
                freshnessStatus = FreshnessStatus.manual.name.lowercase(),
            ),
        )
        videoRepository.saveAll(videos)
    }

    private fun namedUuid(seed: String): UUID =
        UUID.nameUUIDFromBytes(seed.toByteArray(StandardCharsets.UTF_8))
}
