package com.annie.music.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.annie.music.api.Track
import com.annie.music.ui.MusicViewModel

@Composable
fun SearchScreen(viewModel: MusicViewModel) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.searchResults.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = query,
            onValueChange = {
                query = it
                if (it.length >= 3) {
                    viewModel.search(it)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Search songs...") },
            singleLine = true
        )

        LazyColumn {
            items(results) { track ->
                SearchTrackItem(track, viewModel::playTrack)
            }
        }
    }
}

@Composable
fun SearchTrackItem(track: Track, onTrackClick: (Track) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { onTrackClick(track) }) {
        AsyncImage(
            model = track.thumbnail,
            contentDescription = null,
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = track.title, style = MaterialTheme.typography.titleMedium)
            Text(text = track.duration ?: "", style = MaterialTheme.typography.bodySmall)
        }
    }
}
