package com.mystarnow.backend.observability

import com.mystarnow.backend.persistence.repository.PlatformSyncMetadataRepository
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Component
class SyncFreshnessHealthIndicator(
    private val platformSyncMetadataRepository: PlatformSyncMetadataRepository,
) : HealthIndicator {
    override fun health(): Health {
        val degraded = platformSyncMetadataRepository.findAll()
            .filter { it.consecutiveFailures >= 5 }
        return if (degraded.isEmpty()) {
            Health.up().withDetail("degradedScopes", 0).build()
        } else {
            Health.down()
                .withDetail("degradedScopes", degraded.size)
                .withDetail("platforms", degraded.map { "${it.platformCode}:${it.resourceScope}" })
                .build()
        }
    }
}

