package com.mystarnow.backend.idol.service

import com.mystarnow.backend.persistence.entity.IdolGroupEntity
import com.mystarnow.backend.persistence.entity.IdolMemberEntity
import com.mystarnow.backend.persistence.entity.YouTubeChannelEntity
import com.mystarnow.backend.persistence.entity.YouTubeVideoEntity
import com.mystarnow.backend.persistence.repository.IdolGroupRepository
import com.mystarnow.backend.persistence.repository.IdolMemberRepository
import com.mystarnow.backend.persistence.repository.YouTubeChannelRepository
import com.mystarnow.backend.persistence.repository.YouTubeVideoRepository
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class IdolCatalogReadSupport(
    private val groupRepository: IdolGroupRepository,
    private val memberRepository: IdolMemberRepository,
    private val channelRepository: YouTubeChannelRepository,
    private val videoRepository: YouTubeVideoRepository,
) {
    fun loadActiveGroupAggregates(): List<GroupAggregate> {
        val groups = groupRepository.findAllByStatusAndDeletedAtIsNull("active")
        return buildGroupAggregates(groups)
    }

    fun loadActiveGroupAggregateBySlug(groupSlug: String): GroupAggregate? {
        val group = groupRepository.findBySlugAndStatusAndDeletedAtIsNull(groupSlug, "active") ?: return null
        return buildGroupAggregates(listOf(group)).firstOrNull()
    }

    fun loadActiveMemberAggregateBySlug(memberSlug: String): MemberAggregate? {
        val member = memberRepository.findBySlugAndStatusAndDeletedAtIsNull(memberSlug, "active") ?: return null
        val group = groupRepository.findById(member.groupId).orElse(null) ?: return null
        val channels = channelRepository.findAllByOwnerMemberIdAndDeletedAtIsNull(member.id)
            .filter { it.status == "active" }
            .sortedWith(compareByDescending<YouTubeChannelEntity> { it.primary }.thenBy { it.displayLabel ?: it.externalChannelId })
        val videos = if (channels.isEmpty()) {
            emptyList()
        } else {
            videoRepository.findAllByChannelIdInAndDeletedAtIsNullOrderByPublishedAtDesc(channels.map { it.id })
        }
        return MemberAggregate(
            member = member,
            group = group,
            channels = channels,
            videos = videos,
        )
    }

    private fun buildGroupAggregates(groups: List<IdolGroupEntity>): List<GroupAggregate> {
        if (groups.isEmpty()) return emptyList()

        val groupIds = groups.map { it.id }
        val members = memberRepository.findAllByGroupIdInAndDeletedAtIsNull(groupIds)
            .filter { it.status == "active" }
        val memberIds = members.map { it.id }

        val groupChannels = channelRepository.findAllByOwnerGroupIdInAndDeletedAtIsNull(groupIds)
            .filter { it.status == "active" }
        val memberChannels = if (memberIds.isEmpty()) {
            emptyList()
        } else {
            channelRepository.findAllByOwnerMemberIdInAndDeletedAtIsNull(memberIds)
                .filter { it.status == "active" }
        }
        val allChannels = groupChannels + memberChannels
        val videos = if (allChannels.isEmpty()) {
            emptyList()
        } else {
            videoRepository.findAllByChannelIdInAndDeletedAtIsNullOrderByPublishedAtDesc(allChannels.map { it.id })
        }

        val membersByGroupId = members.groupBy { it.groupId }
        val groupChannelsByGroupId = groupChannels.groupBy { it.ownerGroupId }
        val memberChannelsByMemberId = memberChannels.groupBy { it.ownerMemberId }
        val videosByChannelId = videos.groupBy { it.channelId }

        return groups.map { group ->
            val groupMembers = membersByGroupId[group.id].orEmpty().sortedBy { it.sortOrder }
            val officialChannels = groupChannelsByGroupId[group.id].orEmpty()
                .sortedWith(compareByDescending<YouTubeChannelEntity> { it.primary }.thenBy { it.displayLabel ?: it.externalChannelId })
            val memberOwnedChannels = groupMembers.flatMap { member ->
                memberChannelsByMemberId[member.id].orEmpty()
            }
            val groupVideos = (officialChannels + memberOwnedChannels)
                .flatMap { videosByChannelId[it.id].orEmpty() }
                .sortedWith(
                    compareByDescending<YouTubeVideoEntity> { it.publishedAt }
                        .thenByDescending { it.createdAt },
                )

            GroupAggregate(
                group = group,
                members = groupMembers,
                officialChannels = officialChannels,
                memberChannels = memberOwnedChannels,
                videos = groupVideos,
            )
        }
    }
}

data class GroupAggregate(
    val group: IdolGroupEntity,
    val members: List<IdolMemberEntity>,
    val officialChannels: List<YouTubeChannelEntity>,
    val memberChannels: List<YouTubeChannelEntity>,
    val videos: List<YouTubeVideoEntity>,
) {
    val latestVideoAt: OffsetDateTime?
        get() = videos.maxOfOrNull { it.publishedAt }
}

data class MemberAggregate(
    val member: IdolMemberEntity,
    val group: IdolGroupEntity,
    val channels: List<YouTubeChannelEntity>,
    val videos: List<YouTubeVideoEntity>,
)

