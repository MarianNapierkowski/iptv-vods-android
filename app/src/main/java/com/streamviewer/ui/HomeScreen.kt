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
import kotlinx.coroutines.launch

// TODO Add Search
// TODO Add "View All" Button for each Category -> Opens new Screen, dedicated to that Category
// TODO Add Watchlist and Favorites

@Composable
fun HomeScreen(onStreamSelected: (Int, String) -> Unit, onSettingsClick: () -> Unit) {
    var selectedType by remember { mutableStateOf("movies") } // "movies" or "series"
    var categories by remember { mutableStateOf<Map<String, List<StreamEntry>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedType) {
        isLoading = true
        try {
            val response = NetworkClient.getApi().loadCategories(selectedType)
            if (response.isSuccessful) {
                categories = response.body() ?: emptyMap()
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
                enabled = selectedType == "movies"
            ) { Text("Movies") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { selectedType = "series" },
                enabled = selectedType == "series"
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
