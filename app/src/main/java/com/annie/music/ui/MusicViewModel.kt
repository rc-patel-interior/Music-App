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
import kotlinx.coroutines.delay
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

    private val _trendingTracks   = MutableStateFlow<TrendingResponse?>(null)
    val trendingTracks: StateFlow<TrendingResponse?> = _trendingTracks.asStateFlow()

    private val _searchResults    = MutableStateFlow<List<Track>>(emptyList())
    val searchResults: StateFlow<List<Track>> = _searchResults.asStateFlow()

    private val _currentTrack     = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _trackColor       = MutableStateFlow(Color(0xFFE040FB))
    val trackColor: StateFlow<Color> = _trackColor.asStateFlow()

    private val _isPlaying        = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLoading        = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isBuffering      = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _currentPosition  = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration         = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _shuffleEnabled   = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode       = MutableStateFlow(0)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _queue            = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private var _queueIndex       = -1

    private val _favorites        = MutableStateFlow<Map<String, Track>>(emptyMap())
    val favorites: StateFlow<Map<String, Track>> = _favorites.asStateFlow()

    private val _recentTracks     = MutableStateFlow<List<Track>>(emptyList())
    val recentTracks: StateFlow<List<Track>> = _recentTracks.asStateFlow()

    private val _suggestedTracks  = MutableStateFlow<List<Track>>(emptyList())
    val suggestedTracks: StateFlow<List<Track>> = _suggestedTracks.asStateFlow()

    private val _errorMessage     = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            _isPlaying.value = playing
        }
        override fun onPlaybackStateChanged(state: Int) {
            _isBuffering.value = (state == Player.STATE_BUFFERING)
            if (state == Player.STATE_ENDED) {
                when (_repeatMode.value) {
                    1    -> { player.seekTo(0); player.play() }
                    else -> skipToNext()
                }
            }
        }
    }

    init {
        player.addListener(playerListener)
        fetchTrending()
        startPositionTracking()
    }

    private fun startPositionTracking() {
        viewModelScope.launch {
            while (true) {
                _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
                val dur = player.duration
                if (dur > 0L && dur != Long.MIN_VALUE + 1) _duration.value = dur
                delay(500L)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.removeListener(playerListener)
    }

    fun fetchTrending() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTrending()
                .onSuccess { _trendingTracks.value = it }
                .onFailure { _errorMessage.value = "Could not load trending. Check connection." }
            _isLoading.value = false
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.search(query).onSuccess { _searchResults.value = it }
            _isLoading.value = false
        }
    }

    fun fetchSuggested(videoId: String) {
        viewModelScope.launch {
            repository.getSuggested(videoId).onSuccess { tracks ->
                _suggestedTracks.value = tracks.filter { it.id != videoId }
            }
        }
    }

    fun playTrack(track: Track, queue: List<Track> = emptyList()) {
        if (track.id.isBlank()) return
        if (queue.isNotEmpty()) {
            _queue.value = queue
        } else if (_queue.value.none { it.id == track.id }) {
            _queue.value = listOf(track)
        }
        _queueIndex = _queue.value.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        addToRecent(track)
        _suggestedTracks.value = emptyList()
        viewModelScope.launch {
            _currentTrack.value = track
            _isBuffering.value  = true
            updateTrackColor(track.thumbnail)
            repository.getStream(track.id)
                .onSuccess { streamInfo ->
                    val mediaItem = MediaItem.fromUri(streamInfo.url)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                    fetchSuggested(track.id)
                    if (!streamInfo.artist.isNullOrBlank() && track.artist.isNullOrBlank()) {
                        _currentTrack.value = track.copy(
                            title  = streamInfo.title.ifBlank { track.title },
                            artist = streamInfo.artist
                        )
                    }
                }
                .onFailure {
                    _isBuffering.value = false
                    _errorMessage.value = "Could not play this song. Try another."
                }
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(posMs: Long) {
        player.seekTo(posMs.coerceAtLeast(0L))
        _currentPosition.value = posMs.coerceAtLeast(0L)
    }

    fun skipToNext() {
        val q = _queue.value
        if (q.isEmpty()) return
        val nextIdx = when {
            _shuffleEnabled.value    -> (q.indices.toList() - _queueIndex).randomOrNull() ?: return
            _queueIndex < q.size - 1 -> _queueIndex + 1
            _repeatMode.value == 2   -> 0
            else                     -> return
        }
        _queueIndex = nextIdx
        playTrack(q[nextIdx], q)
    }

    fun skipToPrevious() {
        val q = _queue.value
        if (player.currentPosition > 3000L) { player.seekTo(0L); return }
        if (q.isEmpty()) return
        val prevIdx = when {
            _queueIndex > 0          -> _queueIndex - 1
            _repeatMode.value == 2   -> q.size - 1
            else                     -> return
        }
        _queueIndex = prevIdx
        playTrack(q[prevIdx], q)
    }

    fun toggleShuffle() {
        _shuffleEnabled.value = !_shuffleEnabled.value
    }

    fun cycleRepeatMode() {
        _repeatMode.value = (_repeatMode.value + 1) % 3
    }

    fun toggleFavorite(track: Track) {
        val current = _favorites.value.toMutableMap()
        if (track.id in current) current.remove(track.id) else current[track.id] = track
        _favorites.value = current
    }

    fun isFavorite(trackId: String): Boolean = trackId in _favorites.value

    private fun addToRecent(track: Track) {
        val list = _recentTracks.value.toMutableList()
        list.removeAll { it.id == track.id }
        list.add(0, track)
        _recentTracks.value = list.take(30)
    }

    fun downloadTrack(track: Track) {
        viewModelScope.launch {
            repository.getDownloadUrl(track.id).onSuccess { info ->
                val req = DownloadManager.Request(Uri.parse(info.url))
                    .setTitle(track.title)
                    .setDescription("Downloading…")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, "${track.title}.m4a")
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
            }
        }
    }

    fun clearError() { _errorMessage.value = null }

    private suspend fun updateTrackColor(url: String?) {
        if (url == null) return
        try {
            val req    = ImageRequest.Builder(context).data(url).allowHardware(false).build()
            val result = context.imageLoader.execute(req)
            if (result is SuccessResult) {
                val bmp = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                bmp?.let {
                    Palette.from(it).generate { p ->
                        p?.vibrantSwatch?.rgb?.let { c -> _trackColor.value = Color(c) }
                            ?: p?.dominantSwatch?.rgb?.let { c -> _trackColor.value = Color(c) }
                    }
                }
            }
        } catch (_: Exception) {}
    }
}
