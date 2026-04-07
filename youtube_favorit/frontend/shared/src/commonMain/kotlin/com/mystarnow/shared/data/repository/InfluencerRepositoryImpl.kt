package com.mystarnow.shared.data.repository

import com.mystarnow.shared.data.remote.MyStarNowApiProvider
import com.mystarnow.shared.data.remote.toInfluencerDetailDomain
import com.mystarnow.shared.data.remote.toInfluencerListDomain
import com.mystarnow.shared.domain.model.InfluencerDetail
import com.mystarnow.shared.domain.model.InfluencerListPage
import com.mystarnow.shared.domain.model.SearchInfluencersQuery
import com.mystarnow.shared.domain.repository.InfluencerRepository

class InfluencerRepositoryImpl(
    private val apiProvider: MyStarNowApiProvider,
) : InfluencerRepository {
    override suspend fun search(query: SearchInfluencersQuery): InfluencerListPage =
        apiProvider.getApi().getInfluencers(
            query = query.query,
            category = query.category,
            platforms = query.platforms,
            sort = query.sort,
            cursor = query.cursor,
            limit = query.limit,
        ).toInfluencerListDomain()

    override suspend fun getDetail(
        slug: String,
        timezone: String?,
        activitiesLimit: Int,
        schedulesLimit: Int,
    ): InfluencerDetail = apiProvider.getApi().getInfluencerDetail(
        slug = slug,
        timezone = timezone,
        activitiesLimit = activitiesLimit,
        schedulesLimit = schedulesLimit,
    ).toInfluencerDetailDomain()
}
