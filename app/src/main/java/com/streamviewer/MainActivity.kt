package com.streamviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.streamviewer.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StreamViewerApp()
                }
            }
        }
    }
}

@Composable
fun StreamViewerApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onStreamSelected = { streamId, type ->
                    // type: "movie" or "series"
                    navController.navigate("detail/$type/$streamId")
                },
                onSettingsClick = {
                    navController.navigate("sync")
                }
            )
        }
        composable("sync") {
            CategorySyncScreen(onBack = { navController.popBackStack() })
        }
        composable(
            "detail/{type}/{id}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("id") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "movie"
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            DetailScreen(
                type = type,
                id = id,
                onPlay = { url ->
                    navController.navigate("player?url=$url")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "player?url={url}",
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            PlayerScreen(url = url, onBack = { navController.popBackStack() })
        }
    }
}
