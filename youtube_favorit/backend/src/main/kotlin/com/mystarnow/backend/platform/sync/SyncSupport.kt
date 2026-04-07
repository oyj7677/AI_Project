package com.mystarnow.backend.platform.sync

import com.mystarnow.backend.common.config.AppProperties
import com.mystarnow.backend.persistence.entity.PlatformSyncMetadataEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

object SyncStatuses {
    const val IDLE = "idle"
    const val SUCCESS = "success"
    const val FAILED = "failed"
    const val PARTIAL = "partial"
}

data class SyncDecision(
    val shouldRun: Boolean,
    val reason: String,
)

@Component
class SyncBackoffPolicy(
    private val appProperties: AppProperties,
) {
    fun baseMinutes(scope: String): Long = when (scope) {
        com.mystarnow.backend.platform.integration.SyncScopes.CHANNEL_LIVE -> appProperties.youtube.livePollMinutes
        com.mystarnow.backend.platform.integration.SyncScopes.CHANNEL_ACTIVITY -> appProperties.youtube.activityPollMinutes
        else -> appProperties.youtube.channelMetadataPollMinutes
    }

    fun staleAfterMinutes(scope: String): Long = when (scope) {
        com.mystarnow.backend.platform.integration.SyncScopes.CHANNEL_LIVE -> appProperties.reliability.liveStaleAfterMinutes
        com.mystarnow.backend.platform.integration.SyncScopes.CHANNEL_ACTIVITY -> appProperties.reliability.activityStaleAfterMinutes
        else -> appProperties.reliability.profileStaleAfterMinutes
    }

    fun nextSuccess(scope: String, now: OffsetDateTime): OffsetDateTime =
        now.plusMinutes(baseMinutes(scope))

    fun nextFailure(scope: String, now: OffsetDateTime, consecutiveFailures: Int): OffsetDateTime {
        val base = baseMinutes(scope)
        val multiplier = when {
            consecutiveFailures >= 5 -> 10
            consecutiveFailures >= 3 -> 5
            consecutiveFailures >= 2 -> 2
            else -> 1
        }
        return now.plusMinutes(base * multiplier)
    }

    fun shouldRun(metadata: PlatformSyncMetadataEntity, now: OffsetDateTime): SyncDecision {
        if (metadata.backoffUntil?.isAfter(now) == true) {
            return SyncDecision(false, "backoff")
        }
        if (metadata.nextScheduledAt?.isAfter(now) == true) {
            return SyncDecision(false, "scheduled-later")
        }
        return SyncDecision(true, "due")
    }

    fun isDegraded(metadata: PlatformSyncMetadataEntity, now: OffsetDateTime): Boolean {
        if (metadata.lastStatus != SyncStatuses.SUCCESS) {
            return true
        }
        if (metadata.backoffUntil?.isAfter(now) == true) {
            return true
        }
        val lastSucceeded = metadata.lastSucceededAt ?: return true
        return lastSucceeded.plusMinutes(staleAfterMinutes(metadata.resourceScope)).isBefore(now)
    }
}
