package com.streamviewer.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import com.streamviewer.api.NetworkClient
import com.streamviewer.data.Episode
import com.streamviewer.data.Season
import com.streamviewer.data.FavoriteRequest
import com.streamviewer.data.PlaybackEntry
import com.streamviewer.data.PlaybackSaveRequest
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// TODO Add Title to DetailScreen

@Composable
fun DetailScreen(
    type: String, // "movie" or "series"
    id: Int,
    onPlay: (String, Long) -> Unit, // Updated signature
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

    // Movie Data
    var plot by remember { mutableStateOf<String?>(null) }
    var cast by remember { mutableStateOf<String?>(null) }
    var director by remember { mutableStateOf<String?>(null) }

    // Series Data
    var seasons by remember { mutableStateOf<List<Season>>(emptyList()) }
    var episodesMap by remember { mutableStateOf<Map<String, List<Episode>>>(emptyMap()) }
    var selectedSeasonNum by remember { mutableStateOf<Int?>(null) }

    // User State
    var isFavorite by remember { mutableStateOf(false) }
    var playbackEntry by remember { mutableStateOf<PlaybackEntry?>(null) }
    var activeEpisode by remember { mutableStateOf<Episode?>(null) }
    var showOverwriteDialog by remember { mutableStateOf(false) }
    var pendingPlayUrl by remember { mutableStateOf<String?>(null) }

    val mediaKey = "$type:$id"

    // Fetch Details & User State
    LaunchedEffect(id) {
        try {
            // Load Content Details
            if (type == "movie") {
                val response = NetworkClient.getApi().getMovieInfo(mapOf("stream_id" to id))
                if (response.isSuccessful) {
                    val body = response.body()
                    plot = body?.get("plot")
                    cast = body?.get("cast")
                    director = body?.get("director")
                }
            } else {
                val response = NetworkClient.getApi().getSeriesInfo(mapOf("series_id" to id))
                if (response.isSuccessful) {
                    val body = response.body()
                    seasons = body?.seasons ?: emptyList()
                    episodesMap = body?.episodes ?: emptyMap()
                    if (seasons.isNotEmpty()) selectedSeasonNum = seasons.first().seasonNumber
                }
            }

            // Check Favorites
            val favResponse = NetworkClient.getApi().getFavorites(if(type=="series") "series" else "movies")
            if (favResponse.isSuccessful) {
                isFavorite = favResponse.body()?.any { it.media_key == mediaKey } == true
            }

            // Check Watchlist
            val watchResponse = NetworkClient.getApi().getWatchlist(if(type=="series") "series" else "movies")
             if (watchResponse.isSuccessful) {
                 val entries = watchResponse.body() ?: emptyList()
                 playbackEntry = entries.find { it.media_key == mediaKey }

                 // If series, try to find the active episode and set season
                 if (type == "series" && playbackEntry != null) {
                     val entryFile = playbackEntry?.file
                     if (entryFile != null) {
                        // Iterate all episodes to find matching file/ID
                        // Assuming file contains episode ID e.g. "123.mkv" or just "123"
                        var foundEpisode: Episode? = null

                        // We need to search through all values in episodesMap
                        for ((_, eps) in episodesMap) {
                            foundEpisode = eps.find { ep ->
                                // Check if entryFile matches episode ID
                                // entryFile might be "123" or "123.mp4"
                                entryFile == "${ep.id}.${ep.containerExtension}" || entryFile == "${ep.id}" || entryFile.startsWith("${ep.id}.")
                            }
                            if (foundEpisode != null) break
                        }

                        if (foundEpisode != null) {
                            activeEpisode = foundEpisode
                            selectedSeasonNum = foundEpisode.season
                        }
                     }
                 }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (showOverwriteDialog) {
        AlertDialog(
            onDismissRequest = { showOverwriteDialog = false },
            title = { Text("Overwrite current Playback?") },
            text = { Text("Starting this episode will overwrite your current progress in another episode.") },
            confirmButton = {
                Button(
                    onClick = {
                        showOverwriteDialog = false
                        pendingPlayUrl?.let { url ->
                            onPlay(url, 0L)
                        }
                    }
                ) {
                    Text("Yes, Overwrite")
                }
            },
            dismissButton = {
                Button(onClick = { showOverwriteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            if (type == "movie" && !isLoading) {
                val resumeTime = playbackEntry?.position?.times(1000)?.toLong() ?: 0L
                FloatingActionButton(onClick = {
                    val rawUrl = "${NetworkClient.xtreamUrl}/movie/${NetworkClient.username}/${NetworkClient.password}/$id.mkv"
                    val encodedUrl = URLEncoder.encode(rawUrl, StandardCharsets.UTF_8.toString())
                    onPlay(encodedUrl, resumeTime)
                }) {
                    Text(if (resumeTime > 0) "Resume" else "Play")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Row: Title + Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (type == "movie") "Movie Details" else "Series Details",
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.weight(1f)
                        )

                        // Action Buttons
                        Row {
                            IconButton(onClick = {
                                scope.launch {
                                    if (isFavorite) {
                                        NetworkClient.getApi().removeFavorite(mediaKey)
                                        isFavorite = false
                                    } else {
                                        NetworkClient.getApi().addFavorite(FavoriteRequest(mediaKey))
                                        isFavorite = true
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Toggle Favorite",
                                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(onClick = {
                                scope.launch {
                                    if (isInWatchlist) {
                                        NetworkClient.getApi().removePlayback(mediaKey)
                                        isInWatchlist = false
                                    } else {
                                        // Adding to watchlist manually usually sets position to 0?
                                        NetworkClient.getApi().savePlayback(
                                            PlaybackSaveRequest(mediaKey, 0.0, 0.0, null)
                                        )
                                        isInWatchlist = true
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = if (isInWatchlist) Icons.Default.PlaylistAddCheck else Icons.Default.PlaylistAdd,
                                    contentDescription = "Toggle Watchlist",
                                    tint = if (isInWatchlist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!plot.isNullOrEmpty()) {
                        Text("Plot", style = MaterialTheme.typography.titleMedium)
                        Text(plot ?: "")
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (!cast.isNullOrEmpty()) {
                        Text("Cast: $cast")
                    }
                    if (!director.isNullOrEmpty()) {
                        Text("Director: $director")
                    }

                    if (type == "series") {
                        if (activeEpisode != null && playbackEntry != null) {
                            val posSeconds = playbackEntry?.position?.toLong() ?: 0L
                            val playbackPositionMs = playbackEntry?.position?.times(1000)?.toLong() ?: 0L
                            val hours = posSeconds / 3600
                            val minutes = (posSeconds % 3600) / 60
                            val seconds = posSeconds % 60
                            val timeString = if (hours > 0) {
                                String.format("%02d:%02d:%02d", hours, minutes, seconds)
                            } else {
                                String.format("%02d:%02d", minutes, seconds)
                            }

                            Button(
                                onClick = {
                                    val ep = activeEpisode!!
                                    val rawUrl = "${NetworkClient.xtreamUrl}/series/${NetworkClient.username}/${NetworkClient.password}/${ep.id}.${ep.containerExtension}"
                                    val encodedUrl = URLEncoder.encode(rawUrl, StandardCharsets.UTF_8.toString())
                                    onPlay(encodedUrl, playbackPositionMs)
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            ) {
                                Text("Resume S${activeEpisode!!.season}E${activeEpisode!!.episodeNum} $timeString")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Seasons", style = MaterialTheme.typography.titleLarge)

                        // Season Selector
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                            seasons.forEach { season ->
                                Button(
                                    onClick = { selectedSeasonNum = season.seasonNumber },
                                    modifier = Modifier.padding(end = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if(selectedSeasonNum == season.seasonNumber) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text(season.name)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Episodes", style = MaterialTheme.typography.titleLarge)

                        val currentEpisodes = episodesMap[selectedSeasonNum.toString()] ?: emptyList()
                        currentEpisodes.forEach { ep ->
                            val isResumeEpisode = activeEpisode?.id == ep.id

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                         val rawUrl = "${NetworkClient.xtreamUrl}/series/${NetworkClient.username}/${NetworkClient.password}/${ep.id}.${ep.containerExtension}"
                                         val encodedUrl = URLEncoder.encode(rawUrl, StandardCharsets.UTF_8.toString())

                                         if (activeEpisode != null && activeEpisode!!.id != ep.id) {
                                             pendingPlayUrl = encodedUrl
                                             showOverwriteDialog = true
                                         } else if (activeEpisode != null && activeEpisode!!.id == ep.id) {
                                             // Resume same episode
                                             val playbackPositionMs = playbackEntry?.position?.times(1000)?.toLong() ?: 0L
                                             onPlay(encodedUrl, playbackPositionMs)
                                         } else {
                                             // New start
                                             onPlay(encodedUrl, 0L)
                                         }
                                    },
                                border = if (isResumeEpisode) BorderStroke(2.dp, Color.Blue) else null
                            ) {
                                Text(
                                    text = "${ep.episodeNum}. ${ep.title}",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Back Button
            Button(onClick = onBack, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Text("Back")
            }
        }
    }
}
