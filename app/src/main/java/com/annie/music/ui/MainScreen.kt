package com.annie.music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.annie.music.api.Track
import com.annie.music.ui.screens.*
import com.annie.music.ui.screens.player.FullPlayerScreen
import com.annie.music.ui.theme.*

@Composable
fun MainScreen(viewModel: MusicViewModel = hiltViewModel()) {
    val navController   = rememberNavController()
    val currentTrack   by viewModel.currentTrack.collectAsState()
    val isPlaying      by viewModel.isPlaying.collectAsState()
    val isBuffering    by viewModel.isBuffering.collectAsState()
    val currentPos     by viewModel.currentPosition.collectAsState()
    val duration       by viewModel.duration.collectAsState()
    val errorMessage   by viewModel.errorMessage.collectAsState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute    = backStackEntry?.destination?.route

    val navItems = listOf(
        Triple("home",    "Home",    Icons.Rounded.Home),
        Triple("search",  "Search",  Icons.Rounded.Search),
        Triple("library", "Library", Icons.Rounded.LibraryMusic),
    )

    errorMessage?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    Scaffold(
        bottomBar = {
            Column {
                AnimatedVisibility(
                    visible = currentTrack != null && currentRoute != "player",
                    enter   = slideInVertically { it },
                    exit    = slideOutVertically { it }
                ) {
                    currentTrack?.let { track ->
                        MiniPlayer(
                            track      = track,
                            isPlaying  = isPlaying,
                            isBuffering= isBuffering,
                            position   = currentPos,
                            duration   = duration,
                            onTogglePlay = viewModel::togglePlayPause,
                            onSkipNext   = viewModel::skipToNext,
                            onClick      = { navController.navigate("player") }
                        )
                    }
                }
                NavigationBar(
                    containerColor = Surface1,
                    tonalElevation = 0.dp
                ) {
                    navItems.forEach { (route, label, icon) ->
                        val selected = currentRoute == route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (selected) Accent else TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    text  = label,
                                    color = if (selected) Accent else TextSecondary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = selected,
                            onClick  = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = AccentSoft
                            )
                        )
                    }
                }
            }
        },
        containerColor = BgDark,
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = "home",
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable("home")    { HomeScreen(viewModel, onNavigateToPlayer = { navController.navigate("player") }) }
            composable("search")  { SearchScreen(viewModel, onNavigateToPlayer = { navController.navigate("player") }) }
            composable("library") { LibraryScreen(viewModel, onNavigateToPlayer = { navController.navigate("player") }) }
            composable("player")  {
                FullPlayerScreen(
                    viewModel = viewModel,
                    onBack    = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun MiniPlayer(
    track       : Track,
    isPlaying   : Boolean,
    isBuffering : Boolean,
    position    : Long,
    duration    : Long,
    onTogglePlay: () -> Unit,
    onSkipNext  : () -> Unit,
    onClick     : () -> Unit
) {
    val progress = if (duration > 0L) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Surface2, Surface1)
                )
            )
            .clickable { onClick() }
    ) {
        LinearProgressIndicator(
            progress    = { progress },
            modifier    = Modifier.fillMaxWidth().height(2.dp),
            color       = Accent,
            trackColor  = Surface3,
        )
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model          = track.thumbnail,
                contentDescription = null,
                modifier       = Modifier
                    .size(44.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale   = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = track.title,
                    style    = MaterialTheme.typography.titleMedium,
                    color    = TextPrimary,
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
            if (isBuffering) {
                CircularProgressIndicator(
                    modifier  = Modifier.size(24.dp),
                    color     = Accent,
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(onClick = onTogglePlay) {
                    Icon(
                        imageVector        = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint               = TextPrimary,
                        modifier           = Modifier.size(28.dp)
                    )
                }
            }
            IconButton(onClick = onSkipNext) {
                Icon(
                    imageVector        = Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    tint               = TextSecondary,
                    modifier           = Modifier.size(24.dp)
                )
            }
        }
    }
}
