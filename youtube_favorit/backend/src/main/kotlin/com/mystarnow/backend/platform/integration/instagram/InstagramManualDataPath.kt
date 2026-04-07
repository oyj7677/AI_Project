package com.mystarnow.backend.platform.integration.instagram

import com.mystarnow.backend.persistence.readmodel.ActivityRecord
import com.mystarnow.backend.persistence.readmodel.ChannelRecord
import com.mystarnow.backend.persistence.readmodel.InfluencerAggregate
import org.springframework.stereotype.Component

@Component
class InstagramManualDataPath {
    fun channels(aggregate: InfluencerAggregate): List<ChannelRecord> =
        aggregate.channels.filter { it.platform == "instagram" }

    fun activities(aggregate: InfluencerAggregate): List<ActivityRecord> =
        aggregate.recentActivities.filter { it.platform == "instagram" }
}

