package com.mystarnow.shared.data.remote

interface MyStarNowApiProvider {
    suspend fun getApi(): MyStarNowApi
}
