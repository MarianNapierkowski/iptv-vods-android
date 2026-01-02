package com.streamviewer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.streamviewer.api.NetworkClient
import com.streamviewer.data.StreamEntry

// TODO Add Search
// TODO Add "View All" Button for each Category -> Only Entries of that Category are rendered

@Composable
fun HomeScreen(onStreamSelected: (Int, String) -> Unit, onSettingsClick: () -> Unit) {
    var selectedType by remember { mutableStateOf("movies") } // "movies" or "series"
    var categories by remember { mutableStateOf<Map<String, List<StreamEntry>>>(emptyMap()) }
    var watchlist by remember { mutableStateOf<List<StreamEntry>>(emptyList()) }
    var favorites by remember { mutableStateOf<List<StreamEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(selectedType) {
        isLoading = true
        try {
            // Load Categories
            val categoriesResp = NetworkClient.getApi().loadCategories(selectedType)
            if (categoriesResp.isSuccessful) {
                categories = categoriesResp.body() ?: emptyMap()
            }

            // Load Watchlist
            try {
                val watchlistResp = NetworkClient.getApi().getWatchlist(selectedType)
                watchlist = if (watchlistResp.isSuccessful) {
                    watchlistResp.body()?.map { it.toStreamEntry() } ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                watchlist = emptyList()
            }

            // Load Favorites
            try {
                val favoritesResp = NetworkClient.getApi().getFavorites(selectedType)
                if (favoritesResp.isSuccessful) {
                    favorites = favoritesResp.body()?.map { it.toStreamEntry() } ?: emptyList()
                } else {
                    favorites = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                favorites = emptyList()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar / Type Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { selectedType = "movies" },
                enabled = selectedType != "movies"
            ) { Text("Movies") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { selectedType = "series" },
                enabled = selectedType != "series"
            ) { Text("Series") }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings / Sync")
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Watchlist Section
                if (watchlist.isNotEmpty()) {
                    item {
                        Text(
                            text = "Watchlist",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(watchlist) { stream ->
                                StreamCard(stream = stream, onClick = {
                                    onStreamSelected(stream.getId(), if(selectedType == "series") "series" else "movie")
                                })
                            }
                        }
                    }
                }

                // Favorites Section
                if (favorites.isNotEmpty()) {
                    item {
                        Text(
                            text = "Favorites",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(favorites) { stream ->
                                StreamCard(stream = stream, onClick = {
                                    onStreamSelected(stream.getId(), if(selectedType == "series") "series" else "movie")
                                })
                            }
                        }
                    }
                }

                // Categories
                categories.forEach { (catName, streams) ->
                    item {
                        Text(
                            text = catName,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(streams) { stream ->
                                StreamCard(stream = stream, onClick = {
                                    onStreamSelected(stream.getId(), if(selectedType == "series") "series" else "movie")
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StreamCard(stream: StreamEntry, onClick: () -> Unit) {
    // TODO Add Title to StreamCard
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(180.dp)
            .clickable(onClick = onClick)
            .focusable(), // Essential for D-Pad
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            AsyncImage(
                model = stream.getImageUrl(),
                contentDescription = stream.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Gradient or Text overlay if needed
        }
    }
}
