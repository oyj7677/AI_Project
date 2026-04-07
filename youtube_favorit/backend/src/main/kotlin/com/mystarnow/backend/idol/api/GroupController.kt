package com.mystarnow.backend.idol.api

import com.mystarnow.backend.idol.service.GroupQueryService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1/groups")
class GroupController(
    private val groupQueryService: GroupQueryService,
) {
    @GetMapping
    fun getGroups(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) sort: String?,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false) limit: Int?,
        request: HttpServletRequest,
    ) = groupQueryService.getGroups(
        requestId = request.getAttribute("requestId")?.toString() ?: UUID.randomUUID().toString(),
        q = q,
        sort = sort,
        cursor = cursor,
        limit = limit,
    )

    @GetMapping("/{groupSlug}")
    fun getGroupDetail(
        @PathVariable groupSlug: String,
        @RequestParam(required = false) videosCursor: String?,
        @RequestParam(required = false) videosLimit: Int?,
        @RequestParam(required = false) contentType: String?,
        request: HttpServletRequest,
    ) = groupQueryService.getGroupDetail(
        requestId = request.getAttribute("requestId")?.toString() ?: UUID.randomUUID().toString(),
        groupSlug = groupSlug,
        videosCursor = videosCursor,
        videosLimit = videosLimit,
        contentType = contentType,
    )
}
