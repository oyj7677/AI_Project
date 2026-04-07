package com.mystarnow.backend.meta.api

import com.mystarnow.backend.meta.service.AppConfigService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1/meta")
class MetaController(
    private val appConfigService: AppConfigService,
) {
    @GetMapping("/app-config")
    fun getAppConfig(
        @RequestParam clientPlatform: String,
        @RequestParam(required = false) clientVersion: String?,
        @RequestParam(required = false) locale: String?,
        request: HttpServletRequest,
    ) = appConfigService.getAppConfig(
        requestId = request.getAttribute("requestId")?.toString() ?: UUID.randomUUID().toString(),
        clientPlatform = clientPlatform,
        clientVersion = clientVersion,
    )
}
