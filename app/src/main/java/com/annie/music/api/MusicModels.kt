package com.annie.music.api

import com.squareup.moshi.Json

data class Track(
    @Json(name = "v")         val id: String,
    @Json(name = "title")     val title: String,
    @Json(name = "artist")    val artist: String? = null,
    @Json(name = "thumbnail") val thumbnail: String?,
    @Json(name = "duration")  val duration: String? = null,
    @Json(name = "url")       val url: String? = null
)

data class TrendingResponse(
    @Json(name = "hindi")         val hindi: List<Track>?,
    @Json(name = "punjabi")       val punjabi: List<Track>?,
    @Json(name = "bollywood")     val bollywood: List<Track>?,
    @Json(name = "romantic")      val romantic: List<Track>?,
    @Json(name = "international") val international: List<Track>?
)

data class StreamInfo(
    @Json(name = "title")     val title: String,
    @Json(name = "artist")    val artist: String? = null,
    @Json(name = "thumbnail") val thumbnail: String,
    @Json(name = "url")       val url: String,
    @Json(name = "duration")  val duration: String? = null
)
