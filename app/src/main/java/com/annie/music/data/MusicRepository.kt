package com.annie.music.data

import com.annie.music.api.MusicApiService
import com.annie.music.api.StreamInfo
import com.annie.music.api.Track
import com.annie.music.api.TrendingResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val apiService: MusicApiService
) {
    suspend fun search(query: String): Result<List<Track>> = runCatching {
        apiService.search(query)
    }

    suspend fun getTrending(): Result<TrendingResponse> = runCatching {
        apiService.getTrending()
    }

    suspend fun getSuggested(videoId: String): Result<List<Track>> = runCatching {
        apiService.getSuggested(videoId)
    }

    suspend fun getStream(videoId: String): Result<StreamInfo> = runCatching {
        apiService.getStream(videoId)
    }

    suspend fun getDownloadUrl(videoId: String): Result<StreamInfo> = runCatching {
        apiService.getDownloadUrl(videoId)
    }
}
