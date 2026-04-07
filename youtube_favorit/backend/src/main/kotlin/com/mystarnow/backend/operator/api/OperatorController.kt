package com.mystarnow.backend.operator.api

import com.mystarnow.backend.operator.service.OperatorWriteService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/operator")
class OperatorController(
    private val operatorWriteService: OperatorWriteService,
) {
    @PostMapping("/groups")
    fun createGroup(
        @Valid @RequestBody request: GroupCreateRequest,
    ): OperatorMutationResponse = operatorWriteService.createGroup(request)

    @PutMapping("/groups/{groupSlug}")
    fun updateGroup(
        @PathVariable groupSlug: String,
        @Valid @RequestBody request: GroupUpdateRequest,
    ): OperatorMutationResponse = operatorWriteService.updateGroup(groupSlug, request)

    @PostMapping("/members")
    fun createMember(
        @Valid @RequestBody request: MemberCreateRequest,
    ): OperatorMutationResponse = operatorWriteService.createMember(request)

    @PutMapping("/members/{memberSlug}")
    fun updateMember(
        @PathVariable memberSlug: String,
        @Valid @RequestBody request: MemberUpdateRequest,
    ): OperatorMutationResponse = operatorWriteService.updateMember(memberSlug, request)

    @PostMapping("/channels")
    fun createChannel(
        @Valid @RequestBody request: ChannelCreateRequest,
    ): OperatorMutationResponse = operatorWriteService.createChannel(request)

    @PutMapping("/channels/{channelId}")
    fun updateChannel(
        @PathVariable channelId: String,
        @Valid @RequestBody request: ChannelUpdateRequest,
    ): OperatorMutationResponse = operatorWriteService.updateChannel(channelId, request)

    @PostMapping("/videos")
    fun createVideo(
        @Valid @RequestBody request: VideoCreateRequest,
    ): OperatorMutationResponse = operatorWriteService.createVideo(request)

    @PutMapping("/videos/{videoId}")
    fun updateVideo(
        @PathVariable videoId: String,
        @Valid @RequestBody request: VideoUpdateRequest,
    ): OperatorMutationResponse = operatorWriteService.updateVideo(videoId, request)
}
