package com.annie.music.api

import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApiService {
    @GET("api/search")
    suspend fun search(@Query("q") query: String): List<Track>

    @GET("api/trending")
    suspend fun getTrending(): TrendingResponse

    @GET("api/suggested")
    suspend fun getSuggested(@Query("v") videoId: String): List<Track>

    @GET("api/stream")
    suspend fun getStream(@Query("v") videoId: String): StreamInfo

    @GET("api/download")
    suspend fun getDownloadUrl(@Query("v") videoId: String): StreamInfo

    companion object {
        const val BASE_URL = "https://d7e5ab2e-d069-43ca-bddc-1cb2d7817280-00-l75p44txq17x.pike.replit.dev/"
    }
}
