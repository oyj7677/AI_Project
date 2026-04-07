package com.mystarnow.backend.common.web

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class RequestIdFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestId = request.getHeader("X-Request-Id") ?: UUID.randomUUID().toString()
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId)
        response.setHeader("X-Request-Id", requestId)
        filterChain.doFilter(request, response)
    }

    companion object {
        const val REQUEST_ID_ATTRIBUTE = "requestId"
    }
}

