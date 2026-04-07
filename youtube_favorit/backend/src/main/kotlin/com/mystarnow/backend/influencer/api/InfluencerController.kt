package com.mystarnow.backend.influencer.api

import com.mystarnow.backend.influencer.service.InfluencerQueryService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1/influencers")
class InfluencerController(
    private val influencerQueryService: InfluencerQueryService,
) {
    @GetMapping
    fun getInfluencers(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) platform: List<String>?,
        @RequestParam(defaultValue = "featured") sort: String,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "20") limit: Int,
        request: HttpServletRequest,
    ) = influencerQueryService.getInfluencers(
        requestId = request.getAttribute("requestId")?.toString() ?: UUID.randomUUID().toString(),
        q = q,
        category = category,
        platforms = platform,
        sort = sort,
        cursor = cursor,
        limit = limit,
    )

    @GetMapping("/{slug}")
    fun getInfluencerDetail(
        @PathVariable slug: String,
        @RequestParam(required = false) timezone: String?,
        @RequestParam(defaultValue = "20") activitiesLimit: Int,
        @RequestParam(defaultValue = "10") schedulesLimit: Int,
        request: HttpServletRequest,
    ) = influencerQueryService.getInfluencerDetail(
        requestId = request.getAttribute("requestId")?.toString() ?: UUID.randomUUID().toString(),
        slug = slug,
        timezone = timezone,
        activitiesLimit = activitiesLimit,
        schedulesLimit = schedulesLimit,
    )
}
