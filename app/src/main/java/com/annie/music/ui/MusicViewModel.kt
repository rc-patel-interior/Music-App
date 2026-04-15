package com.annie.music.ui

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.annie.music.api.Track
import com.annie.music.api.TrendingResponse
import com.annie.music.data.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val repository: MusicRepository,
    val player: ExoPlayer,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _trendingTracks = MutableStateFlow<TrendingResponse?>(null)
    val trendingTracks: StateFlow<TrendingResponse?> = _trendingTracks.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
    val searchResults: StateFlow<List<Track>> = _searchResults.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _trackColor = MutableStateFlow(Color(0xFF1DB954))
    val trackColor: StateFlow<Color> = _trackColor.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
    }

    init {
        player.addListener(playerListener)
        fetchTrending()
    }

    override fun onCleared() {
        super.onCleared()
        player.removeListener(playerListener)
    }

    fun fetchTrending() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTrending().onSuccess {
                _trendingTracks.value = it
            }
            _isLoading.value = false
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.search(query).onSuccess {
                _searchResults.value = it
            }
            _isLoading.value = false
        }
    }

    fun playTrack(track: Track) {
        viewModelScope.launch {
            _currentTrack.value = track
            updateTrackColor(track.thumbnail)
            repository.getStream(track.id).onSuccess { streamInfo ->
                val mediaItem = MediaItem.fromUri(streamInfo.url)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
            }
        }
    }

    private suspend fun updateTrackColor(thumbnailUrl: String?) {
        if (thumbnailUrl == null) return
        val loader = context.imageLoader
        val request = ImageRequest.Builder(context)
            .data(thumbnailUrl)
            .allowHardware(false)
            .build()
        
        val result = loader.execute(request)
        if (result is SuccessResult) {
            val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            bitmap?.let {
                Palette.from(it).generate { palette ->
                    palette?.dominantSwatch?.rgb?.let { colorInt ->
                        _trackColor.value = Color(colorInt)
                    }
                }
            }
        }
    }

    fun downloadTrack(track: Track) {
        viewModelScope.launch {
            repository.getDownloadUrl(track.id).onSuccess { streamInfo ->
                val request = DownloadManager.Request(Uri.parse(streamInfo.url))
                    .setTitle(track.title)
                    .setDescription("Downloading track...")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, "${track.title}.mp3")
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.enqueue(request)
            }
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }
}
