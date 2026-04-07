package com.mystarnow.backend.idol.api

import com.mystarnow.backend.idol.service.MemberQueryService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1/members")
class MemberController(
    private val memberQueryService: MemberQueryService,
) {
    @GetMapping("/{memberSlug}")
    fun getMemberDetail(
        @PathVariable memberSlug: String,
        @RequestParam(required = false) videosCursor: String?,
        @RequestParam(required = false) videosLimit: Int?,
        @RequestParam(required = false) contentType: String?,
        request: HttpServletRequest,
    ) = memberQueryService.getMemberDetail(
        requestId = request.getAttribute("requestId")?.toString() ?: UUID.randomUUID().toString(),
        memberSlug = memberSlug,
        videosCursor = videosCursor,
        videosLimit = videosLimit,
        contentType = contentType,
    )
}
