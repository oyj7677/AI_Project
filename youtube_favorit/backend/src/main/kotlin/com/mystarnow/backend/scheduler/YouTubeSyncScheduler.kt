package com.mystarnow.backend.scheduler

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import com.mystarnow.backend.platform.sync.YouTubeSyncService

@Component
@EnableScheduling
@ConditionalOnProperty(prefix = "app.youtube", name = ["sync-enabled"], havingValue = "true")
class YouTubeSyncScheduler(
    private val youTubeSyncService: YouTubeSyncService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${app.youtube.live-poll-minutes:3}m")
    fun pollLiveStatus() {
        log.info("running scheduled YouTube live sync")
        youTubeSyncService.syncDueLiveStatus()
    }

    @Scheduled(fixedDelayString = "\${app.youtube.activity-poll-minutes:15}m")
    fun pollActivities() {
        log.info("running scheduled YouTube activity sync")
        youTubeSyncService.syncDueActivities()
    }

    @Scheduled(fixedDelayString = "\${app.youtube.channel-metadata-poll-minutes:720}m")
    fun pollChannelProfiles() {
        log.info("running scheduled YouTube channel profile sync")
        youTubeSyncService.syncDueChannelProfiles()
    }
}
