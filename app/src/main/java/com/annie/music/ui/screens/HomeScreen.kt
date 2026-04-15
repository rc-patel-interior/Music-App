package com.annie.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.annie.music.api.Track
import com.annie.music.ui.MusicViewModel
import com.annie.music.ui.theme.*
import java.util.Calendar

@Composable
fun HomeScreen(viewModel: MusicViewModel, onNavigateToPlayer: () -> Unit) {
    val trending    by viewModel.trendingTracks.collectAsState()
    val isLoading   by viewModel.isLoading.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()

    val categories = listOf("All", "Hindi", "Punjabi", "Bollywood", "Romantic", "International")
    var selectedCat by remember { mutableStateOf("All") }

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11  -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else      -> "Good Night"
        }
    }

    LazyColumn(
        modifier            = Modifier
            .fillMaxSize()
            .background(BgDark),
        contentPadding      = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AccentSoft, BgDark)
                        )
                    )
                    .padding(start = 20.dp, end = 20.dp, top = 52.dp, bottom = 24.dp)
            ) {
                Column {
                    Text(
                        text  = greeting,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "What do you feel like\nlistening to?",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                }
                IconButton(
                    onClick  = viewModel::fetchTrending,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = TextSecondary)
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val selected = cat == selectedCat
                    FilterChip(
                        selected = selected,
                        onClick  = { selectedCat = cat },
                        label    = {
                            Text(
                                text  = cat,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) Color.White else TextSecondary
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor   = Accent,
                            containerColor           = Surface2
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled          = true,
                            selected         = selected,
                            selectedBorderColor = Color.Transparent,
                            borderColor         = Surface4
                        )
                    )
                }
            }
        }

        if (isLoading && trending == null) {
            item {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
            }
        }

        trending?.let { t ->
            val sections = buildList {
                if (selectedCat == "All" || selectedCat == "Hindi")
                    t.hindi?.let { add("Hindi Hits" to it) }
                if (selectedCat == "All" || selectedCat == "Punjabi")
                    t.punjabi?.let { add("Punjabi Beats" to it) }
                if (selectedCat == "All" || selectedCat == "Bollywood")
                    t.bollywood?.let { add("Bollywood Trending" to it) }
                if (selectedCat == "All" || selectedCat == "Romantic")
                    t.romantic?.let { add("Romantic Mood" to it) }
                if (selectedCat == "All" || selectedCat == "International")
                    t.international?.let { add("International Charts" to it) }
            }

            sections.forEach { (title, tracks) ->
                item {
                    TrendingSection(
                        title          = title,
                        tracks         = tracks,
                        currentTrackId = currentTrack?.id,
                        onTrackClick   = { track ->
                            viewModel.playTrack(track, tracks)
                            onNavigateToPlayer()
                        }
                    )
                }
            }

            if (sections.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tracks in this category", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun TrendingSection(
    title         : String,
    tracks        : List<Track>,
    currentTrackId: String?,
    onTrackClick  : (Track) -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text     = title,
            style    = MaterialTheme.typography.titleLarge,
            color    = TextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tracks, key = { it.id }) { track ->
                TrendingCard(
                    track     = track,
                    isPlaying = track.id == currentTrackId,
                    onClick   = { onTrackClick(track) }
                )
            }
        }
    }
}

@Composable
fun TrendingCard(track: Track, isPlaying: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(152.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(if (isPlaying) AccentSoft else Surface2)
            .clickable { onClick() }
            .padding(bottom = 10.dp)
    ) {
        Box {
            AsyncImage(
                model              = track.thumbnail,
                contentDescription = track.title,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(152.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale       = ContentScale.Crop
            )
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(152.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Accent.copy(alpha = 0.6f))
                            )
                        )
                )
                Icon(
                    imageVector        = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(20.dp)
                )
            }
            track.duration?.let { dur ->
                if (dur.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(dur, style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text     = track.title,
            style    = MaterialTheme.typography.bodyMedium,
            color    = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        if (!track.artist.isNullOrBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text     = track.artist,
                style    = MaterialTheme.typography.bodySmall,
                color    = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
        }
    }
}
