package com.streamviewer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.streamviewer.api.NetworkClient
import com.streamviewer.data.Category
import kotlinx.coroutines.launch

// TODO Add Search to Category Sync Table

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySyncScreen(onBack: () -> Unit) {
    var selectedType by remember { mutableStateOf("movies") } // "movies" or "series"
    var allCategories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Fetch categories when type changes
    LaunchedEffect(selectedType) {
        isLoading = true
        errorMessage = null
        try {
            val response = NetworkClient.getApi().getAllCategories(selectedType)
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                allCategories = list
                // Pre-populate selectedIds from the 'preselected' field
                selectedIds = list.filter { it.preselected }.map { it.categoryId }.toSet()
            } else {
                errorMessage = "Failed to load categories: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Category Sync") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Type Switcher
                    TextButton(onClick = { selectedType = "movies" }) {
                        Text("Movies", color = if (selectedType == "movies") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    }
                    TextButton(onClick = { selectedType = "series" }) {
                        Text("Series", color = if (selectedType == "series") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    }

                    // Save Button
                    IconButton(onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                // Filter list to get only selected Category objects
                                val selectedObjects = allCategories.filter { selectedIds.contains(it.categoryId) }
                                val response = NetworkClient.getApi().setCategories(selectedType, selectedObjects)
                                if (response.isSuccessful) {
                                    // Maybe show success toast?
                                    // For now just reload
                                } else {
                                    errorMessage = "Save failed: ${response.code()}"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error saving: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(allCategories) { category ->
                        val isSelected = selectedIds.contains(category.categoryId)
                        ListItem(
                            headlineContent = { Text(category.categoryName) },
                            leadingContent = {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedIds = if (checked) {
                                            selectedIds + category.categoryId
                                        } else {
                                            selectedIds - category.categoryId
                                        }
                                    }
                                )
                            },
                            modifier = Modifier.clickable {
                                // Toggle selection
                                selectedIds = if (isSelected) {
                                    selectedIds - category.categoryId
                                } else {
                                    selectedIds + category.categoryId
                                }
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}
