package com.annie.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.annie.music.api.Track
import com.annie.music.ui.MusicViewModel
import com.annie.music.ui.theme.*

@Composable
fun LibraryScreen(viewModel: MusicViewModel, onNavigateToPlayer: () -> Unit) {
    val recentTracks by viewModel.recentTracks.collectAsState()
    val favorites    by viewModel.favorites.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()

    val favTrackList = favorites.values.toList().reversed()

    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .background(BgDark),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Spacer(Modifier.height(48.dp))
            Text(
                text     = "Your Library",
                style    = MaterialTheme.typography.headlineLarge,
                color    = TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        if (recentTracks.isEmpty() && favTrackList.isEmpty()) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(top = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint     = TextTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Your library is empty", color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Play songs to build your history",
                            color = TextTertiary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        if (favTrackList.isNotEmpty()) {
            item {
                LibrarySectionHeader(title = "Favorites", icon = { Icon(Icons.Rounded.FavoriteBorder, null, tint = Accent, modifier = Modifier.size(20.dp)) })
            }
            items(favTrackList, key = { "fav_${it.id}" }) { track ->
                LibraryTrackItem(
                    track     = track,
                    isPlaying = track.id == currentTrack?.id,
                    onClick   = {
                        viewModel.playTrack(track, favTrackList)
                        onNavigateToPlayer()
                    }
                )
            }
        }

        if (recentTracks.isNotEmpty()) {
            item {
                LibrarySectionHeader(title = "Recently Played", icon = { Icon(Icons.Rounded.History, null, tint = AccentAlt, modifier = Modifier.size(20.dp)) })
            }
            items(recentTracks, key = { "rec_${it.id}" }) { track ->
                LibraryTrackItem(
                    track     = track,
                    isPlaying = track.id == currentTrack?.id,
                    onClick   = {
                        viewModel.playTrack(track, recentTracks)
                        onNavigateToPlayer()
                    }
                )
            }
        }
    }
}

@Composable
private fun LibrarySectionHeader(title: String, icon: @Composable () -> Unit) {
    Row(
        modifier          = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icon()
        Text(
            text  = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )
    }
}

@Composable
private fun LibraryTrackItem(track: Track, isPlaying: Boolean, onClick: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isPlaying) AccentSoft else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            AsyncImage(
                model              = track.thumbnail,
                contentDescription = null,
                modifier           = Modifier
                    .size(52.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale       = ContentScale.Crop
            )
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(Accent.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.MusicNote, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
        }
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
                Spacer(Modifier.height(2.dp))
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
                Text(it, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            }
        }
    }
}
