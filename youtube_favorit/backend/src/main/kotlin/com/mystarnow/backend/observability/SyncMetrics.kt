package com.mystarnow.backend.observability

import com.mystarnow.backend.persistence.repository.ActivityItemRepository
import com.mystarnow.backend.persistence.repository.LiveStatusCacheRepository
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class SyncMetrics(
    meterRegistry: MeterRegistry,
    private val liveStatusCacheRepository: LiveStatusCacheRepository,
    private val activityItemRepository: ActivityItemRepository,
) {
    private val successCounters = mutableMapOf<String, Counter>()
    private val failureCounters = mutableMapOf<String, Counter>()

    init {
        Gauge.builder("mystarnow.sync.stale_ratio.live") {
            val total = liveStatusCacheRepository.count().toDouble()
            if (total == 0.0) 0.0 else liveStatusCacheRepository.countByFreshnessStatus("stale").toDouble() / total
        }.register(meterRegistry)

        Gauge.builder("mystarnow.sync.stale_ratio.activity") {
            val total = activityItemRepository.count().toDouble()
            if (total == 0.0) 0.0 else activityItemRepository.countByFreshnessStatus("stale").toDouble() / total
        }.register(meterRegistry)
    }

    fun recordSuccess(platform: String, scope: String) {
        val key = "$platform:$scope"
        successCounters.getOrPut(key) {
            Counter.builder("mystarnow.sync.success")
                .tag("platform", platform)
                .tag("scope", scope)
                .register(io.micrometer.core.instrument.Metrics.globalRegistry)
        }.increment()
    }

    fun recordFailure(platform: String, scope: String) {
        val key = "$platform:$scope"
        failureCounters.getOrPut(key) {
            Counter.builder("mystarnow.sync.failure")
                .tag("platform", platform)
                .tag("scope", scope)
                .register(io.micrometer.core.instrument.Metrics.globalRegistry)
        }.increment()
    }
}

