package com.annie.music.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.annie.music.api.Track
import com.annie.music.ui.MusicViewModel

@Composable
fun HomeScreen(viewModel: MusicViewModel) {
    val trending = viewModel.trendingTracks.collectAsState().value

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "Trending",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        trending?.hindi?.let {
            item { TrendingSection("Hindi Trending", it, viewModel::playTrack) }
        }
        trending?.punjabi?.let {
            item { TrendingSection("Punjabi Trending", it, viewModel::playTrack) }
        }
        trending?.international?.let {
            item { TrendingSection("International Trending", it, viewModel::playTrack) }
        }
    }
}

@Composable
fun TrendingSection(title: String, tracks: List<Track>, onTrackClick: (Track) -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tracks) { track ->
                TrackItem(track, onTrackClick)
            }
        }
    }
}

@Composable
fun TrackItem(track: Track, onTrackClick: (Track) -> Unit) {
    Column(modifier = Modifier.width(150.dp).clickable { onTrackClick(track) }) {
        AsyncImage(
            model = track.thumbnail,
            contentDescription = track.title,
            modifier = Modifier.size(150.dp),
            contentScale = ContentScale.Crop
        )
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
