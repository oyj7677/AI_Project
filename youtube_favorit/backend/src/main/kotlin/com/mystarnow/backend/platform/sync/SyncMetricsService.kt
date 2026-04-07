package com.mystarnow.backend.platform.sync

import com.mystarnow.backend.persistence.repository.ActivityItemRepository
import com.mystarnow.backend.persistence.repository.LiveStatusCacheRepository
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.stereotype.Component

@Component
class SyncMetricsService(
    meterRegistry: MeterRegistry,
    private val liveStatusCacheRepository: LiveStatusCacheRepository,
    private val activityItemRepository: ActivityItemRepository,
) {
    private val registry = meterRegistry

    init {
        Gauge.builder("mystarnow.cache.live.stale.ratio") {
            ratio(
                total = liveStatusCacheRepository.count(),
                stale = liveStatusCacheRepository.countByFreshnessStatus("stale"),
            )
        }.register(registry)

        Gauge.builder("mystarnow.cache.activity.stale.ratio") {
            ratio(
                total = activityItemRepository.count(),
                stale = activityItemRepository.countByFreshnessStatus("stale"),
            )
        }.register(registry)
    }

    fun recordSync(platform: String, scope: String, result: String) {
        registry.counter(
            "mystarnow.sync.execution",
            Tags.of("platform", platform, "scope", scope, "result", result),
        ).increment()
    }

    fun recordSectionDegraded(section: String, platform: String, scope: String) {
        registry.counter(
            "mystarnow.section.degraded",
            Tags.of("section", section, "platform", platform, "scope", scope),
        ).increment()
    }

    private fun ratio(total: Long, stale: Long): Double =
        if (total == 0L) 0.0 else stale.toDouble() / total.toDouble()
}

