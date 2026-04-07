package com.mystarnow.backend.scheduler

import com.mystarnow.backend.idol.platform.youtube.IdolYouTubeSyncService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
@ConditionalOnProperty(prefix = "app.youtube", name = ["sync-enabled"], havingValue = "true")
class IdolYouTubeSyncScheduler(
    private val idolYouTubeSyncService: IdolYouTubeSyncService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${app.youtube.activity-poll-minutes:15}m")
    fun syncVideos() {
        val synced = idolYouTubeSyncService.syncAllActiveChannels()
        log.info("idol youtube scheduled sync complete syncedVideos={}", synced)
    }
}
