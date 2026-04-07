package com.mystarnow.shared.data.repository

import com.mystarnow.shared.data.remote.MyStarNowApiProvider
import com.mystarnow.shared.data.remote.toDomain
import com.mystarnow.shared.domain.model.HomeFeed
import com.mystarnow.shared.domain.repository.HomeRepository

class HomeRepositoryImpl(
    private val apiProvider: MyStarNowApiProvider,
) : HomeRepository {
    override suspend fun getHomeFeed(timezone: String?, locale: String?): HomeFeed =
        apiProvider.getApi().getHomeFeed(timezone = timezone, locale = locale).toDomain()
}
