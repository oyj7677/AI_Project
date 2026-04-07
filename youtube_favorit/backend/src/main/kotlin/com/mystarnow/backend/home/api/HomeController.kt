package com.mystarnow.backend.home.api

import com.mystarnow.backend.home.service.HomeQueryService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1/home")
class HomeController(
    private val homeQueryService: HomeQueryService,
) {
    @GetMapping
    fun getHome(
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) contentType: String?,
        request: HttpServletRequest,
    ) = homeQueryService.getHome(
        requestId = request.getAttribute("requestId")?.toString() ?: UUID.randomUUID().toString(),
        cursor = cursor,
        limit = limit,
        contentType = contentType,
    )
}
