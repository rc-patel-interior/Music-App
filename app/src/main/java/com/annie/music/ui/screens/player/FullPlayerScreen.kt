package com.annie.music.ui.screens.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.annie.music.api.Track
import com.annie.music.ui.MusicViewModel
import com.annie.music.ui.theme.*

@Composable
fun FullPlayerScreen(
    viewModel: MusicViewModel,
    onBack   : () -> Unit
) {
    val track       by viewModel.currentTrack.collectAsState()
    val isPlaying   by viewModel.isPlaying.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val trackColor  by viewModel.trackColor.collectAsState()
    val position    by viewModel.currentPosition.collectAsState()
    val duration    by viewModel.duration.collectAsState()
    val shuffle     by viewModel.shuffleEnabled.collectAsState()
    val repeatMode  by viewModel.repeatMode.collectAsState()
    val favorites   by viewModel.favorites.collectAsState()
    val suggested   by viewModel.suggestedTracks.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()

    var dragProgress by remember { mutableFloatStateOf(-1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            trackColor.copy(alpha = 0.55f),
                            BgDark
                        )
                    )
                )
        )

        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Spacer(Modifier.height(48.dp))
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Close",
                            tint               = TextPrimary,
                            modifier           = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text  = "Now Playing",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    track?.let { t ->
                        IconButton(onClick = { viewModel.downloadTrack(t) }) {
                            Icon(
                                Icons.Rounded.Download,
                                contentDescription = "Download",
                                tint               = TextSecondary
                            )
                        }
                    } ?: Spacer(Modifier.size(48.dp))
                }
            }

            track?.let { t ->
                item {
                    Spacer(Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model              = t.thumbnail,
                            contentDescription = null,
                            modifier           = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .shadow(32.dp, MaterialTheme.shapes.large)
                                .clip(MaterialTheme.shapes.large),
                            contentScale       = ContentScale.Crop
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(28.dp))
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text     = t.title,
                                style    = MaterialTheme.typography.headlineSmall,
                                color    = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!t.artist.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text     = t.artist,
                                    style    = MaterialTheme.typography.bodyLarge,
                                    color    = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        IconButton(onClick = { viewModel.toggleFavorite(t) }) {
                            Icon(
                                imageVector        = if (favorites.containsKey(t.id))
                                    Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint               = if (favorites.containsKey(t.id)) Accent else TextSecondary
                            )
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(20.dp))
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        val progress = if (dragProgress >= 0f) dragProgress
                        else if (duration > 0L) position.toFloat() / duration.toFloat() else 0f

                        Slider(
                            value         = progress.coerceIn(0f, 1f),
                            onValueChange = { dragProgress = it },
                            onValueChangeFinished = {
                                if (dragProgress >= 0f && duration > 0L) {
                                    viewModel.seekTo((dragProgress * duration).toLong())
                                }
                                dragProgress = -1f
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors   = SliderDefaults.colors(
                                thumbColor        = Color.White,
                                activeTrackColor  = trackColor,
                                inactiveTrackColor = Surface4,
                            )
                        )
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text  = formatMs(if (dragProgress >= 0f && duration > 0L) (dragProgress * duration).toLong() else position),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text  = formatMs(duration),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = viewModel::toggleShuffle) {
                            Icon(
                                Icons.Rounded.Shuffle,
                                contentDescription = "Shuffle",
                                tint               = if (shuffle) trackColor else TextSecondary,
                                modifier           = Modifier.size(24.dp)
                            )
                        }
                        IconButton(
                            onClick  = viewModel::skipToPrevious,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                Icons.Rounded.SkipPrevious,
                                contentDescription = "Previous",
                                tint               = TextPrimary,
                                modifier           = Modifier.size(36.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(trackColor)
                                .clickable { viewModel.togglePlayPause() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isBuffering) {
                                CircularProgressIndicator(
                                    color       = Color.White,
                                    strokeWidth = 3.dp,
                                    modifier    = Modifier.size(32.dp)
                                )
                            } else {
                                Icon(
                                    imageVector        = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint               = Color.White,
                                    modifier           = Modifier.size(36.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick  = viewModel::skipToNext,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                Icons.Rounded.SkipNext,
                                contentDescription = "Next",
                                tint               = TextPrimary,
                                modifier           = Modifier.size(36.dp)
                            )
                        }
                        IconButton(onClick = viewModel::cycleRepeatMode) {
                            Icon(
                                imageVector        = when (repeatMode) {
                                    1    -> Icons.Rounded.RepeatOne
                                    2    -> Icons.Rounded.Repeat
                                    else -> Icons.Rounded.Repeat
                                },
                                contentDescription = "Repeat",
                                tint               = if (repeatMode > 0) trackColor else TextSecondary,
                                modifier           = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                if (suggested.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(28.dp))
                        Text(
                            text     = "Up Next",
                            style    = MaterialTheme.typography.titleLarge,
                            color    = TextPrimary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    items(suggested, key = { "sug_${it.id}" }) { sug ->
                        SuggestedTrackItem(
                            track     = sug,
                            isPlaying = sug.id == currentTrack?.id,
                            onClick   = { viewModel.playTrack(sug, suggested) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestedTrackItem(track: Track, isPlaying: Boolean, onClick: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isPlaying) AccentSoft else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model              = track.thumbnail,
            contentDescription = null,
            modifier           = Modifier
                .size(50.dp)
                .clip(MaterialTheme.shapes.small),
            contentScale       = ContentScale.Crop
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = track.title,
                style    = MaterialTheme.typography.titleMedium,
                color    = if (isPlaying) Accent else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!track.artist.isNullOrBlank()) {
                Text(
                    text     = track.artist,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        track.duration?.let {
            if (it.isNotBlank()) {
                Spacer(Modifier.width(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    val hr  = min / 60
    return if (hr > 0) "$hr:${(min % 60).toString().padStart(2,'0')}:${sec.toString().padStart(2,'0')}"
    else "$min:${sec.toString().padStart(2,'0')}"
}
