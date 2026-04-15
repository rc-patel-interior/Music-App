package com.annie.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Search
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
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(viewModel: MusicViewModel, onNavigateToPlayer: () -> Unit) {
    var query       by remember { mutableStateOf("") }
    val results     by viewModel.searchResults.collectAsState()
    val isLoading   by viewModel.isLoading.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()

    LaunchedEffect(query) {
        if (query.length >= 2) {
            delay(400)
            viewModel.search(query)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            text     = "Search",
            style    = MaterialTheme.typography.headlineLarge,
            color    = TextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        OutlinedTextField(
            value         = query,
            onValueChange = { query = it },
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder   = {
                Text("Songs, artists, albums…", color = TextTertiary)
            },
            leadingIcon   = {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = TextSecondary)
            },
            trailingIcon  = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            singleLine    = true,
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = Accent,
                unfocusedBorderColor = Surface4,
                focusedTextColor     = TextPrimary,
                unfocusedTextColor   = TextPrimary,
                cursorColor          = Accent,
                focusedContainerColor   = Surface2,
                unfocusedContainerColor = Surface2,
            ),
            shape         = MaterialTheme.shapes.large
        )

        when {
            query.isBlank() -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            tint     = TextTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Type to search for music", color = TextTertiary)
                    }
                }
            }
            isLoading && results.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
            }
            results.isEmpty() && query.length >= 2 -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results for \"$query\"", color = TextSecondary)
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(results, key = { it.id }) { track ->
                        SearchTrackItem(
                            track     = track,
                            isPlaying = track.id == currentTrack?.id,
                            onClick   = {
                                viewModel.playTrack(track, results)
                                onNavigateToPlayer()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchTrackItem(track: Track, isPlaying: Boolean, onClick: () -> Unit) {
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
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale       = ContentScale.Crop
            )
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(Accent.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint     = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
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
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!track.artist.isNullOrBlank()) {
                    Text(
                        text     = track.artist,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                track.duration?.let { dur ->
                    if (dur.isNotBlank()) {
                        if (!track.artist.isNullOrBlank()) {
                            Box(
                                Modifier
                                    .padding(horizontal = 6.dp)
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(TextTertiary)
                            )
                        }
                        Text(
                            text  = dur,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}
